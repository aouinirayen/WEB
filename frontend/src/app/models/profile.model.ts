/**
 * User Profile Response DTO
 * Contains all profile information for the current authenticated user
 */
export interface ProfileResponse {
  id: number;
  username: string;
  email: string;
  name: string;
  cin: number;
  role: string;
  photoContentType: string | null;
  photoPath: string | null; // Relative path to photo file (e.g., "1/1234567890_abcdefgh.jpg")
  createdAt: string;
  updatedAt: string;
}

/**
 * User Profile Update Request DTO
 * Used for updating profile information (username, email, name, cin)
 */
export interface ProfileUpdateRequest {
  username?: string;
  email: string;
  name: string;
  cin?: number;
}
