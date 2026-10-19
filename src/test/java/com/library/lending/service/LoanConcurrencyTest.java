package com.library.lending.service;

import com.library.lending.domain.Book;
import com.library.lending.domain.Member;
import com.library.lending.dto.BorrowRequest;
import com.library.lending.exception.NoAvailableCopiesException;
import com.library.lending.repository.BookRepository;
import com.library.lending.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LoanConcurrencyTest {

    @Autowired
    private LoanService loanService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void concurrentBorrows_forSingleCopyBook_onlyOneSucceeds() throws Exception {
        Book book = bookRepository.save(Book.builder()
                .title("Concurrent Book").author("Author").isbn("ISBN-CONC-1")
                .totalCopies(1).availableCopies(1).build());

        int n = 20;
        List<Long> memberIds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Member member = memberRepository.save(Member.builder()
                    .name("Member " + i).email("member" + i + "@example.com").build());
            memberIds.add(member.getId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(n);
        CountDownLatch readyLatch = new CountDownLatch(n);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejectedNoCopies = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        List<Future<?>> futures = new ArrayList<>();
        for (Long memberId : memberIds) {
            futures.add(executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    loanService.borrow(new BorrowRequest(book.getId(), memberId));
                    succeeded.incrementAndGet();
                } catch (NoAvailableCopiesException e) {
                    rejectedNoCopies.incrementAndGet();
                } catch (Exception e) {
                    unexpected.add(e);
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(unexpected).isEmpty();
        assertThat(succeeded.get()).isEqualTo(1);
        assertThat(rejectedNoCopies.get()).isEqualTo(n - 1);

        Book reloaded = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(reloaded.getAvailableCopies()).isEqualTo(0);
    }
}
