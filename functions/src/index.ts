import * as admin from "firebase-admin";

admin.initializeApp();

// Core Functions (Phase 1)
export { createPost } from "./callable/createPost";
export { createComment } from "./callable/createComment";
export { reportContent } from "./callable/reportContent";
export { blockUser, unblockUser } from "./callable/blockUser";

// Booking Functions (Phase 2)
export { createShortlist } from "./callable/createShortlist";
export { createBookingRequest } from "./callable/createBookingRequest";
export { respondToBookingRequest } from "./callable/respondToBookingRequest";

// Chat Functions (Phase 4)
export { createConversationFromAcceptedRequest } from "./callable/createConversationFromAcceptedRequest";
export { sendMessage } from "./callable/sendMessage";

// Contact Sharing (Milestone C)
export { requestContactShare } from "./callable/requestContactShare";
export { approveContactShare } from "./callable/approveContactShare";

// Scheduled Functions
export { cleanupRateLimits } from "./scheduled/cleanupRateLimits";
