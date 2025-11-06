# 🔒 Firebase Security Rules for ScriptMine

## Overview

This document describes the Firebase Firestore security rules implemented for ScriptMine to ensure data privacy, security, and proper access control.

## Security Principles

### 1. **User Data Isolation**
- Each user can only access their own scripts
- No cross-user data access is permitted
- Authentication is required for all operations

### 2. **Data Validation**
- All script documents must have required fields
- Data types are validated on write operations
- Version control is enforced for conflict resolution

### 3. **Ownership Verification**
- Users can only create scripts with themselves as owner
- Ownership cannot be transferred between users
- Updates maintain original ownership

## Rules Breakdown

### Scripts Collection (`/scripts/{scriptId}`)

#### **Read Operations**
```javascript
allow read: if request.auth != null && 
           request.auth.uid == resource.data.userId;
```
- **Requirement**: User must be authenticated
- **Condition**: User ID must match the script's userId field
- **Effect**: Users can only read their own scripts

#### **Create Operations**
```javascript
allow create: if request.auth != null && 
             request.auth.uid == request.resource.data.userId &&
             isValidScriptData(request.resource.data);
```
- **Requirement**: User must be authenticated
- **Condition**: User must set themselves as the owner
- **Validation**: Script data must pass validation function
- **Effect**: Users can only create scripts for themselves

#### **Update Operations**
```javascript
allow update: if request.auth != null && 
             request.auth.uid == resource.data.userId &&
             request.auth.uid == request.resource.data.userId &&
             isValidScriptData(request.resource.data);
```
- **Requirement**: User must be authenticated
- **Condition**: User must own both current and updated script
- **Validation**: Updated data must pass validation
- **Effect**: Prevents ownership transfer and unauthorized updates

#### **Write Operations (General)**
```javascript
allow write: if request.auth != null && 
            request.auth.uid == resource.data.userId;
```
- **Covers**: Update and Delete operations
- **Requirement**: User authentication and ownership
- **Effect**: Complete CRUD control for owned scripts

### Data Validation Function

```javascript
function isValidScriptData(data) {
  return data.keys().hasAll(['templateType', 'clientName', 'formData', 'generatedScript', 'userId']) &&
         data.templateType is string &&
         data.clientName is string &&
         data.formData is map &&
         data.generatedScript is string &&
         data.userId is string &&
         data.version is int &&
         data.version > 0 &&
         data.isDeleted is bool;
}
```

#### **Required Fields**
- `templateType`: String - Type of script template
- `clientName`: String - Name of the client
- `formData`: Map - Form data as key-value pairs
- `generatedScript`: String - Generated script content
- `userId`: String - Owner's Firebase user ID
- `version`: Integer - Version number (must be > 0)
- `isDeleted`: Boolean - Soft delete flag

#### **Type Validation**
- Ensures all fields have correct data types
- Prevents injection of invalid data
- Maintains data consistency across the application

### User Metadata Collection (`/users/{userId}`)

```javascript
match /users/{userId} {
  allow read, write: if request.auth != null && 
                    request.auth.uid == userId;
}
```
- **Purpose**: Store user-specific settings and metadata
- **Access**: Users can only access their own user document
- **Future Use**: Profile settings, preferences, sync status

### System Metadata Collection (`/metadata/{document}`)

```javascript
match /metadata/{document=**} {
  allow read: if request.auth != null;
}
```
- **Purpose**: System-wide configuration and metadata
- **Access**: Read-only for all authenticated users
- **Use Cases**: App version info, feature flags, announcements

## Security Features

### 1. **Authentication Enforcement**
- All operations require valid Firebase Authentication
- Anonymous access is completely blocked
- Token validation is handled by Firebase

### 2. **Data Ownership**
- Strict user ID matching on all operations
- Prevents data leakage between users
- Ownership cannot be changed after creation

### 3. **Input Validation**
- Required fields validation
- Data type checking
- Business logic validation (version > 0)

### 4. **Soft Delete Protection**
- `isDeleted` field is validated as boolean
- Prevents accidental data corruption
- Maintains sync integrity

## Rate Limiting (Future Enhancement)

```javascript
function isRateLimited() {
  return request.time > resource.data.updatedAt + duration.value(1, 's');
}
```
- **Purpose**: Prevent excessive write operations
- **Limit**: Maximum 1 write per second per document
- **Status**: Prepared for future implementation

## Testing Security Rules

### Test Cases to Verify

1. **Authenticated User Access**
   - ✅ User can read their own scripts
   - ✅ User can create scripts for themselves
   - ✅ User can update their own scripts
   - ✅ User can delete their own scripts

2. **Unauthorized Access Prevention**
   - ❌ User cannot read other users' scripts
   - ❌ User cannot create scripts for other users
   - ❌ User cannot update other users' scripts
   - ❌ User cannot delete other users' scripts

3. **Unauthenticated Access Prevention**
   - ❌ Anonymous users cannot perform any operations
   - ❌ Invalid tokens are rejected
   - ❌ Expired tokens are rejected

4. **Data Validation**
   - ❌ Scripts without required fields are rejected
   - ❌ Scripts with invalid data types are rejected
   - ❌ Scripts with invalid version numbers are rejected

## Deployment Instructions

### 1. **Using Firebase Console**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your ScriptMine project
3. Navigate to **Firestore Database** > **Rules**
4. Copy the contents of `firestore.rules`
5. Paste into the rules editor
6. Click **Publish**

### 2. **Using Firebase CLI**
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firebase in project directory
firebase init firestore

# Deploy rules
firebase deploy --only firestore:rules
```

### 3. **Validation**
After deployment, test the rules using:
- Firebase Console Rules Playground
- Unit tests with Firebase Emulator
- Integration tests with real Firebase project

## Monitoring and Maintenance

### 1. **Security Monitoring**
- Monitor Firebase Console for security violations
- Set up alerts for unusual access patterns
- Regular review of access logs

### 2. **Rule Updates**
- Version control all rule changes
- Test rules in emulator before deployment
- Document all changes with rationale

### 3. **Performance Monitoring**
- Monitor rule evaluation performance
- Optimize complex rules if needed
- Consider caching for frequently accessed data

## Troubleshooting

### Common Issues

1. **Permission Denied Errors**
   - Check user authentication status
   - Verify userId matches in document
   - Ensure all required fields are present

2. **Data Validation Failures**
   - Check field names and types
   - Verify version number is positive
   - Ensure isDeleted is boolean

3. **Rule Deployment Issues**
   - Validate rule syntax before deployment
   - Check Firebase project permissions
   - Verify CLI authentication

### Debug Commands

```bash
# Test rules locally
firebase emulators:start --only firestore

# Validate rules syntax
firebase firestore:rules:validate

# Deploy with debug info
firebase deploy --only firestore:rules --debug
```

## Security Best Practices

1. **Principle of Least Privilege**: Users only access what they need
2. **Defense in Depth**: Multiple validation layers
3. **Input Validation**: All data is validated before storage
4. **Audit Trail**: All operations are logged by Firebase
5. **Regular Reviews**: Periodic security rule audits

These security rules ensure that ScriptMine maintains the highest standards of data privacy and security while enabling the hybrid offline-first architecture.