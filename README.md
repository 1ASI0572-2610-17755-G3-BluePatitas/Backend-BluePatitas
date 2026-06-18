# BluePatitas Backend

## Demo credentials

The development seed creates demo data conditionally. It does not delete existing data and does not duplicate users, roles, shelter, animals, feeding plans, or observations when they already exist.

| Role | Email | Password | Shelter |
| --- | --- | --- | --- |
| `ROLE_SHELTER_ADMIN` | `admin@bluepatitas.com` | `admin123` | `WUF Shelter` |
| `ROLE_VETERINARIAN` | `vet@bluepatitas.com` | `vet123` | `WUF Shelter` |

## Authentication

### Sign in

`POST /api/v1/authentication/sign-in`

Request:

```json
{
  "email": "admin@bluepatitas.com",
  "password": "admin123"
}
```

Response keeps the previous fields and adds Web/Mobile role fields:

```json
{
  "id": 1,
  "firstName": "Carlos",
  "lastName": "Admin",
  "email": "admin@bluepatitas.com",
  "token": "jwt",
  "shelterId": "uuid-or-null",
  "role": "SHELTER_ADMIN",
  "roles": ["SHELTER_ADMIN"],
  "shelterName": "WUF Shelter",
  "onboardingCompleted": true
}
```

Internal roles are still stored as `ROLE_SHELTER_ADMIN` and `ROLE_VETERINARIAN`; API login responses expose `SHELTER_ADMIN` and `VETERINARIAN`.

### Public admin registration

`POST /api/v1/authentication/sign-up`

If `role` is omitted, the backend creates a shelter admin. The endpoint accepts `SHELTER_ADMIN` and `ROLE_SHELTER_ADMIN`. Public registration as `VETERINARIAN` or `ROLE_VETERINARIAN` is rejected with HTTP 400.

## Veterinary admin endpoints

### List veterinarians

`GET /api/veterinary/veterinarians`

Returns users with `ROLE_VETERINARIAN` for the current shelter.
Requires `ROLE_SHELTER_ADMIN`.
The `assignedAnimalsCount` field currently represents the number of animals in the veterinarian's shelter, not a granular individual assignment.

### Invite veterinarian

`POST /api/veterinary/veterinarians/invite`

Request:

```json
{
  "email": "new.vet@bluepatitas.com",
  "firstName": "Andrea",
  "lastName": "Torres",
  "password": "vet123"
}
```

Response:

```json
{
  "id": 10,
  "firstName": "Andrea",
  "lastName": "Torres",
  "email": "new.vet@bluepatitas.com",
  "role": "VETERINARIAN",
  "shelterId": "uuid",
  "shelterName": "WUF Shelter",
  "status": "ACTIVE",
  "invitationCode": "VET-BP-2026"
}
```

This is a minimal invitation flow: it creates an active veterinarian user and returns a demo invitation code. Email delivery is not implemented yet.
Requires `ROLE_SHELTER_ADMIN`.

## Veterinary self-service endpoints

### Dashboard

`GET /api/veterinary/me/dashboard`

Returns a minimal dashboard for the authenticated veterinarian.
Requires `ROLE_VETERINARIAN`.

### Animals

`GET /api/veterinary/me/animals`

Returns animals from the veterinarian's shelter. Granular veterinarian-animal assignment is pending.
Requires `ROLE_VETERINARIAN`.

## Current limitations

- There is no dedicated `Veterinarian` entity; veterinarian users are `User` records with `ROLE_VETERINARIAN`.
- There is no granular veterinarian-animal assignment yet; veterinarian animal lists use all animals in the same shelter.
- `VeterinaryObservation.veterinarianId` is still a UUID and is not redesigned in this iteration.
- GPS, camera, Devices, real IoT flows, and real dispenser execution are pending.
- Real email invitations are pending.
- Flyway is present as a dependency but disabled; no migration was added in this iteration.
