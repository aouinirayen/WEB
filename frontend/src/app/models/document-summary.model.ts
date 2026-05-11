export interface ApiError {
  error: string;
}

export interface BackendHealthResponse {
  status: string;
}

export interface DocumentSummaryResponse {
  success: boolean;
  document_type: string;
  extracted_text: string;
  summary: string;
  text_length: number;
  message: string;
}
