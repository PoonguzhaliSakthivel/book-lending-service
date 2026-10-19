CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       isbn VARCHAR(20) NOT NULL,
                       total_copies INT NOT NULL,
                       available_copies INT NOT NULL,
                       CONSTRAINT uq_books_isbn UNIQUE (isbn),
                       CONSTRAINT chk_books_copies CHECK (available_copies >= 0 AND available_copies <= total_copies)
);

CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         CONSTRAINT uq_members_email UNIQUE (email)
);

CREATE TABLE loans (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       book_id BIGINT NOT NULL,
                       member_id BIGINT NOT NULL,
                       borrowed_at TIMESTAMP NOT NULL,
                       due_date TIMESTAMP NOT NULL,
                       returned_at TIMESTAMP NULL,
                       CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books(id),
                       CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE INDEX idx_loans_member_returned ON loans (member_id, returned_at);
CREATE INDEX idx_loans_due_date ON loans (due_date);
