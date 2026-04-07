-- 1) Reference data
INSERT INTO specialities (name) VALUES
                                    ('Обща медицина'),
                                    ('Кардиология'),
                                    ('Вътрешни болести');

INSERT INTO diagnoses (name) VALUES
                                 ('Грип'),
                                 ('Хипертония'),
                                 ('Гастрит');

-- 2) Users — BCrypt for plaintext password: password
INSERT INTO users (username, password, role) VALUES
                                                 ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'),
                                                 ('dr_gp', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR'),
                                                 ('dr_cardio', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'DOCTOR'),
                                                 ('patient_ivan', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PATIENT'),
                                                 ('patient_maria', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PATIENT'),
                                                 ('pending_user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PENDING');

-- 3) Doctors (GP + non-GP covers assign-role / GP reports)
INSERT INTO doctors (name, is_gp, speciality_id, user_id)
SELECT 'Д-р ГП Иванов', true, s.id, u.id
FROM specialities s, users u
WHERE s.name = 'Обща медицина' AND u.username = 'dr_gp';

INSERT INTO doctors (name, is_gp, speciality_id, user_id)
SELECT 'Д-р Кардиолог Петров', false, s.id, u.id
FROM specialities s, users u
WHERE s.name = 'Кардиология' AND u.username = 'dr_cardio';

-- 4) Patients (EGN unique, length 10; each has a GP user)
INSERT INTO patients (name, egn, gp_id, user_id)
SELECT 'Иван Иванов', '9001011234', d.id, u.id
FROM doctors d JOIN users u ON u.username = 'patient_ivan'
WHERE d.name = 'Д-р ГП Иванов';

INSERT INTO patients (name, egn, gp_id, user_id)
SELECT 'Мария Георгиева', '8505054321', d.id, u.id
FROM doctors d JOIN users u ON u.username = 'patient_maria'
WHERE d.name = 'Д-р ГП Иванов';

-- 5) Health insurance — paid/unpaid + unique (patient_id, year, month)
INSERT INTO health_insurances (patient_id, year, month, paid)
SELECT p.id, 2026, 1, true FROM patients p JOIN users u ON p.user_id = u.id WHERE u.username = 'patient_ivan';

INSERT INTO health_insurances (patient_id, year, month, paid)
SELECT p.id, 2026, 2, false FROM patients p JOIN users u ON p.user_id = u.id WHERE u.username = 'patient_ivan';

INSERT INTO health_insurances (patient_id, year, month, paid)
SELECT p.id, 2026, 1, true FROM patients p JOIN users u ON p.user_id = u.id WHERE u.username = 'patient_maria';

-- 6) Examinations — mix PATIENT / NHIF for money reports; varied dates for period filters
INSERT INTO examinations (date_time, doctor_id, patient_id, diagnosis_id, treatment, price, paid_by)
SELECT TIMESTAMPTZ '2026-02-10 09:00:00+02', d.id, p.id, diag.id,
       'Покой, парацетамол при нужда', 45.00, 'PATIENT'
FROM doctors d, patients p, diagnoses diag
WHERE d.name = 'Д-р ГП Иванов'
  AND p.egn = '9001011234'
  AND diag.name = 'Грип';

INSERT INTO examinations (date_time, doctor_id, patient_id, diagnosis_id, treatment, price, paid_by)
SELECT TIMESTAMPTZ '2026-03-05 11:30:00+02', d.id, p.id, diag.id,
       'АНТ хипертензивно', 0.00, 'NHIF'
FROM doctors d, patients p, diagnoses diag
WHERE d.name = 'Д-р Кардиолог Петров'
  AND p.egn = '8505054321'
  AND diag.name = 'Хипертония';

INSERT INTO examinations (date_time, doctor_id, patient_id, diagnosis_id, treatment, price, paid_by)
SELECT TIMESTAMPTZ '2026-03-18 14:00:00+02', d.id, p.id, diag.id,
       'Диета, ИПП', 60.00, 'PATIENT'
FROM doctors d, patients p, diagnoses diag
WHERE d.name = 'Д-р ГП Иванов'
  AND p.egn = '8505054321'
  AND diag.name = 'Гастрит';

-- 7) Sick leaves — same month on several rows makes "month with most sick leaves" interesting
INSERT INTO sick_leaves (start_date, days, examination_id)
SELECT DATE '2026-03-12', 5, e.id
FROM examinations e
         JOIN patients p ON e.patient_id = p.id
WHERE p.egn = '9001011234' AND e.date_time = TIMESTAMPTZ '2026-02-10 09:00:00+02';

INSERT INTO sick_leaves (start_date, days, examination_id)
SELECT DATE '2026-03-20', 3, e.id
FROM examinations e
         JOIN patients p ON e.patient_id = p.id
WHERE p.egn = '8505054321' AND e.date_time = TIMESTAMPTZ '2026-03-18 14:00:00+02';