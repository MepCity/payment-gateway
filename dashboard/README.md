# Payment Gateway Dashboard

A modern React dashboard for managing payment gateway operations, built with Material-UI and TypeScript.

## Features

- **Authentication**: Secure login system with protected routes
- **Process Payments**: Complete payment processing workflow
- **Payment Management**: View and manage all payments
- **Customer Management**: Track customer information and history
- **Refund Management**: Process and track refunds
- **Responsive Design**: Mobile-friendly interface

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn

### Installation

1. Navigate to the dashboard directory:
   ```bash
   cd dashboard
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

### Demo Credentials

- **Email**: admin@example.com
- **Password**: password

## Project Structure

```
dashboard/
├── src/
│   ├── components/
│   │   ├── auth/           # Authentication components
│   │   ├── common/         # Reusable UI components
│   │   ├── layout/         # Layout and navigation
│   │   └── payments/       # Payment-specific components
│   ├── contexts/           # React contexts (Auth)
│   ├── pages/              # Page components
│   ├── services/           # API services
│   └── types/              # TypeScript type definitions
├── public/                 # Static assets
└── package.json           # Dependencies and scripts
```

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm eject` - Eject from Create React App

## Technologies Used

- **React 18** - UI library
- **TypeScript** - Type safety
- **Material-UI (MUI)** - Component library
- **React Router** - Navigation
- **Context API** - State management

## Development

The dashboard is built with modern React patterns and follows best practices:

- Functional components with hooks
- TypeScript for type safety
- Material-UI for consistent design
- Responsive design principles
- Clean component architecture

## Backend Integration

The dashboard is designed to work with the payment gateway backend. API endpoints are configured in the services directory and can be easily modified to match your backend implementation.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is part of the Payment Gateway system.
