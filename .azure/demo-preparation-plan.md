# ADS Dental Demo - Endpoint Preparation Plan (2-Hour Demo)

## Demo Scope: Functional Requirements Only

This document outlines which endpoints are needed for the demo based on functional requirements.

---

## ✅ OFFICE MANAGER Workflows

### Authentication
- **POST** `/api/auth/register` - Register Office Manager (DONE)
- **POST** `/api/auth/login` - Login Office Manager (DONE)

### Dentist Management
- **POST** `/adsweb/api/v1/dentists` - Register/Create new dentist (DONE)
  - Records: ID, name, phone, email, specialization
- **GET** `/adsweb/api/v1/dentists` - View all dentists (DONE)
- **GET** `/adsweb/api/v1/dentist/search/{searchString}` - Search dentists (DONE)

### Patient Management
- **POST** `/adsweb/api/v1/patients` - Enroll new patient (DONE)
  - Records: name, phone, email, address, date of birth
- **GET** `/adsweb/api/v1/patients` - View all patients (DONE)
- **GET** `/adsweb/api/v1/patient/search/{searchString}` - Search patients (DONE)

### Surgery Management
- **POST** `/adsweb/api/v1/surgeries` - Create surgery (DONE)
  - Records: name, address, phone
- **GET** `/adsweb/api/v1/surgeries` - View all surgeries (DONE)

### Appointment Management
- **POST** `/adsweb/api/v1/appointments` - Book appointment (DONE)
- **GET** `/adsweb/api/v1/appointments` - View all appointments (DONE)
- **GET** `/adsweb/api/v1/appointments/{id}` - View appointment details (DONE)

---

## ✅ PATIENT Workflows

### Authentication
- **POST** `/api/auth/register` - Register new patient (DONE)
- **POST** `/api/auth/login` - Patient login (DONE)

### Appointments
- **POST** `/adsweb/api/v1/appointments` - Request new appointment (DONE)
- **GET** `/adsweb/api/v1/appointments/patient/{patientId}` - View all their appointments (DONE)
- **GET** `/adsweb/api/v1/appointments/patient/{patientId}/upcoming` - View upcoming appointments (DONE)
- **GET** `/adsweb/api/v1/appointments/{id}` - View appointment details (DONE)
- **PUT** `/adsweb/api/v1/appointment/{id}/reschedule` - Request appointment change (DONE)
- **POST** `/adsweb/api/v1/appointment/{id}/cancel` - Request cancellation (DONE)
- **DELETE** `/adsweb/api/v1/appointment/{id}` - Cancel appointment (DONE)

### Dentist Information
- **GET** `/adsweb/api/v1/dentists` - View all dentists (DONE)
- **GET** `/adsweb/api/v1/dentists/{id}` - View assigned dentist details (DONE)

---

## ✅ DENTIST Workflows

### Authentication
- **POST** `/api/auth/register` - Register new dentist (DONE)
- **POST** `/api/auth/login` - Dentist login (DONE)

### Appointments
- **GET** `/adsweb/api/v1/appointments/dentist/{dentistId}` - View all assigned appointments (DONE)
- **GET** `/adsweb/api/v1/appointments/dentist/{dentistId}/upcoming` - View upcoming appointments (DONE)
- **GET** `/adsweb/api/v1/appointments/{id}` - View appointment details (DONE)

### Patient Information
- **GET** `/adsweb/api/v1/patients/{id}` - View patient details (DONE)
- **GET** `/adsweb/api/v1/patients` - View all patients (DONE)

---

## 🔍 SYSTEM Validations to Verify

### 1. Maximum 5 appointments per dentist per week
- **Status**: ⚠️ NEEDS VERIFICATION
- **Location**: `AppointmentService.createAppointment()`
- **Action**: Verify validation logic is working

### 2. Check for unpaid bills before allowing new appointment
- **Status**: ⚠️ NEEDS VERIFICATION
- **Location**: `AppointmentService.createAppointment()`
- **Note**: `app.demo.validate-unpaid-bills=false` in properties (currently disabled)
- **Action**: Review if needed for demo

### 3. Email confirmation upon booking
- **Status**: ⚠️ NEEDS VERIFICATION
- **Location**: `EmailService` and `AppointmentService`
- **Configuration**: Email settings in `application.properties`
- **Action**: Verify email sending is configured (may need to use test mode)

### 4. Record and store appointments
- **Status**: ✅ DONE
- **Location**: Database via JPA

---

## 📋 Demo HTTP Files Available

✅ **api_officer_manager_demo.http** - Office Manager workflows
✅ **api_patient_demo.http** - Patient workflows
✅ **api_dentist_demo.http** - Dentist workflows
✅ **api_unauthorized_demo.http** - Authorization tests
✅ **api.http** - General API tests

---

## 🚀 Pre-Demo Checklist

### Database & Configuration
- [ ] PostgreSQL running on `localhost:5432`
- [ ] Database `ads_dental` exists and initialized
- [ ] `application.properties` configured correctly
  - Database credentials
  - Email settings (if testing email notifications)
  - JWT secret configured
  - Demo mode settings

### API Server
- [ ] Application compiled: `mvn clean package`
- [ ] Server running on port `8081`
- [ ] No build errors or warnings

### Testing Endpoints
- [ ] Test Office Manager registration and login
- [ ] Test Dentist creation and listing
- [ ] Test Patient enrollment and listing
- [ ] Test Surgery creation
- [ ] Test Appointment booking with validation
- [ ] Test Patient appointment viewing
- [ ] Test Dentist appointment viewing
- [ ] Test error handling (unauthorized, invalid data, etc.)

### Business Logic Validation
- [ ] Verify 5-appointment weekly limit works
- [ ] Verify unpaid bills check (if enabled)
- [ ] Verify appointment confirmation emails (optional)

---

## ⏱️ Estimated Time Allocation (2 hours)

| Task | Time |
|------|------|
| Database & Configuration Setup | 15 min |
| Start Application | 10 min |
| Test Core Workflows | 30 min |
| Test Edge Cases & Validations | 20 min |
| Live Demo Execution | 45 min |
| **TOTAL** | **120 min** |

---

## 🔧 Quick Fixes (if needed during demo prep)

### If email sending fails:
1. Use dummy/test email configuration
2. Comment out email service calls temporarily

### If unpaid bills check causes issues:
1. Keep `app.demo.validate-unpaid-bills=false`
2. Set all test patients with `hasUnpaidBill=false`

### If appointment limit causes issues:
1. Review validation logic in `AppointmentService`
2. Adjust test dates to avoid hitting the limit

---

## 📝 Demo Flow (Suggested Order)

1. **Setup Demo Data** (via Office Manager)
   - Create surgeries
   - Create dentists
   - Enroll patients

2. **Patient Workflow Demo**
   - Patient login
   - View available dentists
   - Request appointment
   - View appointment confirmation

3. **Dentist Workflow Demo**
   - Dentist login
   - View assigned appointments
   - View patient details

4. **Office Manager Workflow Demo**
   - Manage patients and dentists
   - View all appointments
   - Verify business rules (5-appt limit, etc.)

---

## 🎯 Success Criteria for Demo

- ✅ All three user roles can authenticate
- ✅ All CRUD operations work without errors
- ✅ Role-based access control is enforced
- ✅ Appointment business rules are validated
- ✅ API responses are properly formatted
- ✅ Error handling returns meaningful messages

