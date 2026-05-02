-- Notifications table
CREATE TABLE notifications (
    id                BIGSERIAL PRIMARY KEY,
    recipient_id      VARCHAR(255) NOT NULL,
    recipient_email   VARCHAR(255) NOT NULL,
    recipient_phone   VARCHAR(50),
    type              VARCHAR(50)  NOT NULL,
    channel           VARCHAR(20)  NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    subject           VARCHAR(500) NOT NULL,
    body              TEXT         NOT NULL,
    template_id       VARCHAR(100),
    reference_id      VARCHAR(255),
    reference_type    VARCHAR(100),
    retry_count       INT          NOT NULL DEFAULT 0,
    error_message     TEXT,
    sent_at           TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_status       ON notifications(status);
CREATE INDEX idx_notifications_reference    ON notifications(reference_id, reference_type);
CREATE INDEX idx_notifications_created_at   ON notifications(created_at DESC);
