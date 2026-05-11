/**
 * Document Type Status Enum
 */
export enum DocumentStatusEnum {
  PENDING = 'PENDING',
  VALID = 'VALID',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED',
  REQUEST_UPDATE = 'REQUEST_UPDATE'
}

/**
 * Role Enum
 */
export enum RoleEnum {
  ADMIN = 'ADMIN',
  AGENT = 'AGENT',
  OPERATOR = 'OPERATOR',
  PASSENGER = 'PASSENGER'
}

/**
 * Document Type Interface
 */
export interface DocumentType {
  id: number;
  name: string;
  description: string;
  requiresExpiry: boolean;
  allowedRoles: string; // comma-separated roles
}

/**
 * Legal Document Interface
 */
export interface LegalDocument {
  id: number;
  userId: number;
  username?: string; // Optional username field
  documentTypeId: number; // Helper field derived from documentType.id
  documentType: DocumentType; // Full DocumentType object
  documentUrl: string;
  fileHash: string;
  uploadDate: string; // ISO DateTime
  expiryDate: string | null; // ISO DateTime, nullable
  status: DocumentStatusEnum;
}

/**
 * Document Search Criteria Interface
 */
export interface DocumentSearchCriteria {
  userId?: number;
  documentTypeId?: number;
  status?: DocumentStatusEnum;
  page: number;
  size: number;
}

/**
 * Document Approval Request Interface
 */
export interface DocumentApprovalRequest {
  id: number;
  action: 'approve' | 'reject' | 'request-update';
  reason?: string; // for reject or request-update
  feedback?: string; // for request-update
}

/**
 * Document Type Create/Update Request Interface
 */
export interface DocumentTypeCreateRequest {
  name: string;
  description: string;
  requiresExpiry: boolean;
  allowedRoles: string; // comma-separated string
}

/**
 * Pagination Response Interface
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  last: boolean;
  first: boolean;
  empty: boolean;
}

/**
 * Document Upload Request Interface
 */
export interface DocumentUploadRequest {
  file: File;
  documentTypeId: number;
  expiryDate?: string; // ISO DateTime
}

/**
 * Generic API Response Interface
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

/**
 * Error Response Interface
 */
export interface ErrorResponse {
  success: boolean;
  message: string;
  errors?: Record<string, string>;
  timestamp: string;
  status: number;
}
