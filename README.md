PennyWise Project
PennyWise is a comprehensive personal finance management application that empowers users to manage their financial activities efficiently. With features ranging from transaction tracking to bill management, PennyWise offers an intuitive interface and seamless functionality for daily financial needs.

1) Features

1.1) Authentication
  Sign In: Secure login with email and password.
  Register with Email and Password: Register using Firebase Authentication.
  Email Verification: Verify your email address upon sign-up to enhance security.
  Password Reset: Send an email to reset your password.
  Google Sign-In: Quickly register or log in using your Google account.
  
1.2) Home Page (HomeFragment)
  View Wallet Balance: Get an overview of your current wallet balance.
  Add a Transaction: Record income or expense transactions effortlessly.
  Add a Saving Goal: Set financial goals and track progress toward achieving them.  
  Today's Transactions: View a summary of today’s transactions with an option to filter and view all past transactions.
  Saving Goals Management: Add money to your saving goals and track progress dynamically.
  Exchange Rates: Fetch live exchange rates via an API using Retrofit for currency conversion insights.
  
1.3) Send & Request Money: Use the floating action button to send or request money from other users.

1.4) Navigation Bar:
  Homepage: View and manage transactions, saving goals, and exchange rates.
  Profile Management: Update and view personal information.
  Insights: Visualize income, expenses, and financial goals.
  Bill Management: Automate and track subscriptions and bills.
  
1.5) Profile Management
  Update your profile details, including firstname, lastname and change your password.
  
1.6) Insights Page
  Financial Overview:
  View total income and expenses.
  Financial Health Score: Understand your spending habits and financial status.
  Top Spending Categories: Analyze your spending trends.
  Saving Goals Progress:
  Visualize progress for each saving goal using progress bars.

1.7) Bill Management
  Schedule Bills & Subscriptions:
  Automate payments on scheduled dates if the balance is sufficient.
  Pay manually later if the balance is insufficient.

2) Instructions
Getting Started:

Clone the repository.
Open the project in Android Studio.
Sync Gradle and ensure all dependencies are installed.
API Integration:

Ensure Firebase is properly connected with your project.
Running the App:

Build and run the application on an emulator or a physical device.
Register a user account or log in using your Google account.

Best Practices:

Verify your email after signing up to enable full access.
Ensure sufficient wallet balance for automated bill payments.

Technology Stack
Android Development: Kotlin, Android Studio
Backend: Firebase Authentication, Firebase Firestore
Networking: Retrofit for API calls
UI: ConstraintLayout, RecyclerView, LinearLayout
Third-Party Libraries: Google Sign-In

Contributions
We welcome contributions! If you'd like to add features, fix bugs, or improve the app, feel free to fork the repository and submit a pull request.

For any issues or suggestions, please contact the project maintainers or open an issue in the repository
