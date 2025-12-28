import SwiftUI

struct EmailVerificationView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var email: String = ""
    @State private var isValidating: Bool = false
    @State private var validationMessage: String = ""
    @State private var validationSuccess: Bool = false
    @State private var showDomainList: Bool = false

    var onEmailVerified: ((String) -> Void)?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 12) {
                        Image(systemName: "envelope.badge.shield.half.filled")
                            .font(.system(size: 60))
                            .foregroundStyle(.blue.gradient)

                        Text("Verify Your School Email")
                            .font(.title2)
                            .fontWeight(.bold)

                        Text("Enter your school email address to verify your account and unlock additional features.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                    }
                    .padding(.top, 32)

                    // Email Input
                    VStack(alignment: .leading, spacing: 8) {
                        Text("School Email")
                            .font(.subheadline)
                            .fontWeight(.medium)

                        TextField("student@school.edu", text: $email)
                            .textContentType(.emailAddress)
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .autocorrectionDisabled()
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(validationSuccess ? Color.green : Color.clear, lineWidth: 2)
                            )
                    }
                    .padding(.horizontal)

                    // Validation Message
                    if !validationMessage.isEmpty {
                        HStack(spacing: 8) {
                            Image(systemName: validationSuccess ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                                .foregroundStyle(validationSuccess ? .green : .orange)

                            Text(validationMessage)
                                .font(.subheadline)
                                .foregroundStyle(validationSuccess ? .green : .orange)

                            Spacer()
                        }
                        .padding()
                        .background(validationSuccess ? Color.green.opacity(0.1) : Color.orange.opacity(0.1))
                        .cornerRadius(12)
                        .padding(.horizontal)
                    }

                    // Verify Button
                    Button(action: verifyEmail) {
                        HStack {
                            if isValidating {
                                ProgressView()
                                    .tint(.white)
                            } else {
                                Text("Verify Email")
                                    .fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(email.isEmpty ? Color.gray : Color.blue)
                        .foregroundStyle(.white)
                        .cornerRadius(12)
                    }
                    .disabled(email.isEmpty || isValidating)
                    .padding(.horizontal)

                    // Show Approved Domains
                    Button(action: { showDomainList.toggle() }) {
                        HStack {
                            Image(systemName: "info.circle")
                            Text("View Approved School Domains")
                        }
                        .font(.subheadline)
                        .foregroundStyle(.blue)
                    }

                    if showDomainList {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Approved Domains:")
                                .font(.subheadline)
                                .fontWeight(.semibold)

                            ForEach(Array(SchoolDomainWhitelist.approvedDomains).sorted(), id: \.self) { domain in
                                HStack {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundStyle(.green)
                                        .font(.caption)
                                    Text("@\(domain)")
                                        .font(.footnote)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                        .padding(.horizontal)
                    }

                    Spacer()
                }
            }
            .navigationTitle("Email Verification")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
    }

    private func verifyEmail() {
        isValidating = true
        validationMessage = ""
        validationSuccess = false

        // Simulate network delay for realistic UX
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            validateEmailAddress()
            isValidating = false
        }
    }

    private func validateEmailAddress() {
        // Step 1: Validate email format
        guard SchoolDomainWhitelist.isValidEmailFormat(email) else {
            validationMessage = "Please enter a valid email address"
            validationSuccess = false
            return
        }

        // Step 2: Check if domain is approved
        guard SchoolDomainWhitelist.isApprovedDomain(email) else {
            validationMessage = "This email domain is not approved. Please use a school email address."
            validationSuccess = false
            showDomainList = true
            return
        }

        // Step 3: In local simulation mode, auto-verify
        validationMessage = "Email verified successfully! Your account has been updated."
        validationSuccess = true

        // Notify parent view
        onEmailVerified?(email)

        // Auto-dismiss after success
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            dismiss()
        }
    }
}

#Preview {
    EmailVerificationView()
}
