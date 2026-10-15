CREATE TABLE IF NOT EXISTS member (
    member_id BIGSERIAL PRIMARY KEY,
    member_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    membership_tier VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS night_balance (
    member_id BIGINT PRIMARY KEY,
    available_nights INT NOT NULL
);

CREATE TABLE IF NOT EXISTS redemption_request (
    redemption_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    hotel_name VARCHAR(100) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INT NOT NULL,
    redemption_status VARCHAR(20) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
