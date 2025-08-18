import React, { useState } from 'react';
import { Box, CssBaseline, Toolbar } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';

const DashboardLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      
      {/* Header */}
      <Header 
        sidebarOpen={sidebarOpen} 
        onSidebarToggle={handleSidebarToggle} 
      />
      
      {/* Sidebar */}
      <Sidebar 
        open={sidebarOpen} 
        onToggle={handleSidebarToggle} 
      />
      
      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          bgcolor: 'background.default',
          minHeight: '100vh',
          transition: theme => theme.transitions.create('margin', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
          }),
        }}
      >
        <Toolbar /> {/* Spacer for fixed header */}
        <Box sx={{ p: 3 }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
};

export default DashboardLayout;
