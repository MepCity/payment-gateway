import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  IconButton,
  Button,
  Chip,
  Menu,
  MenuItem,
  Avatar,
  Divider,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Search,
  Notifications,
  AccountCircle,
  ExitToApp,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import ThemeToggle from '../common/ThemeToggle';

interface HeaderProps {
  sidebarOpen: boolean;
  onSidebarToggle: () => void;
}

const Header: React.FC<HeaderProps> = ({ sidebarOpen, onSidebarToggle }) => {
  const { state, logout } = useAuth();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleProfileMenuClose();
    logout();
  };

  return (
    <AppBar 
      position="fixed" 
      sx={{ 
        zIndex: (theme) => theme.zIndex.drawer + 1,
        backgroundColor: 'background.paper',
        color: 'text.primary',
        boxShadow: 1,
        borderBottom: 1,
        borderColor: 'divider'
      }}
      elevation={0}
    >
      <Toolbar sx={{ justifyContent: 'space-between' }}>
        {/* Left Section */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton
            color="inherit"
            aria-label="toggle sidebar"
            onClick={onSidebarToggle}
            edge="start"
          >
            <MenuIcon />
          </IconButton>
          
          {/* CASHFLIX Logo */}
          <Box sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 1,
            cursor: 'pointer',
            mr: 2
          }}>
            <Box sx={{
              width: 36,
              height: 36,
              background: 'linear-gradient(135deg, #E50914 0%, #B81D24 100%)',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(229, 9, 20, 0.3)',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <Typography 
                variant="h6" 
                sx={{ 
                  color: 'white',
                  fontWeight: 900,
                  fontSize: '1.1rem',
                  letterSpacing: '-0.5px',
                  textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                  fontFamily: '"Arial Black", "Helvetica Black", sans-serif'
                }}
              >
                C
              </Typography>
              {/* Netflix-style shine effect */}
              <Box sx={{
                position: 'absolute',
                top: 0,
                left: '-100%',
                width: '100%',
                height: '100%',
                background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent)',
                animation: 'shine 3s infinite',
                '@keyframes shine': {
                  '0%': { left: '-100%' },
                  '50%': { left: '100%' },
                  '100%': { left: '100%' }
                }
              }} />
            </Box>
            <Typography 
              variant="h6" 
              sx={{ 
                fontWeight: 900,
                background: 'linear-gradient(135deg, #E50914 0%, #B81D24 100%)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                letterSpacing: '-1px',
                fontFamily: '"Arial Black", "Helvetica Black", sans-serif',
                textShadow: '0 2px 4px rgba(0,0,0,0.1)',
                display: { xs: 'none', sm: 'block' },
                transform: 'perspective(200px) rotateX(20deg)',
                transformOrigin: 'center center',
                filter: 'drop-shadow(2px 4px 6px rgba(0,0,0,0.3))'
              }}
            >
              CashFlix
            </Typography>
          </Box>
          
          {/* Test Mode Banner - like Hyperswitch */}
          <Chip
            label="You're in Test Mode"
            variant="outlined"
            color="warning"
            size="small"
            sx={{ 
              backgroundColor: 'warning.main',
              color: 'warning.contrastText',
              fontWeight: 500,
              borderColor: 'warning.main'
            }}
          />
        </Box>

        {/* Right Section */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {/* Search - could be implemented later */}
          <IconButton color="inherit" size="large">
            <Search />
          </IconButton>

          {/* Notifications */}
          <IconButton color="inherit" size="large">
            <Notifications />
          </IconButton>

          {/* Theme Toggle */}
          <ThemeToggle />

          {/* Profile Menu */}
          <IconButton
            size="large"
            edge="end"
            aria-label="account of current user"
            aria-controls="profile-menu"
            aria-haspopup="true"
            onClick={handleProfileMenuOpen}
            color="inherit"
          >
            <Avatar sx={{ width: 32, height: 32 }}>
              {state.user?.email.charAt(0).toUpperCase()}
            </Avatar>
          </IconButton>

          <Menu
            id="profile-menu"
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleProfileMenuClose}
            onClick={handleProfileMenuClose}
            PaperProps={{
              elevation: 0,
              sx: {
                overflow: 'visible',
                filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
                mt: 1.5,
                minWidth: 200,
                '& .MuiAvatar-root': {
                  width: 32,
                  height: 32,
                  ml: -0.5,
                  mr: 1,
                },
                '&:before': {
                  content: '""',
                  display: 'block',
                  position: 'absolute',
                  top: 0,
                  right: 14,
                  width: 10,
                  height: 10,
                  bgcolor: 'background.paper',
                  transform: 'translateY(-50%) rotate(45deg)',
                  zIndex: 0,
                },
              },
            }}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            {/* User Info */}
            <Box sx={{ px: 2, py: 1 }}>
              <Typography variant="subtitle2" noWrap>
                {state.user?.email}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {state.user?.merchantName}
              </Typography>
            </Box>
            
            <Divider />
            
            <MenuItem onClick={handleProfileMenuClose}>
              <AccountCircle sx={{ mr: 2 }} />
              Profile Settings
            </MenuItem>
            
            <MenuItem onClick={handleLogout}>
              <ExitToApp sx={{ mr: 2 }} />
              Logout
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;