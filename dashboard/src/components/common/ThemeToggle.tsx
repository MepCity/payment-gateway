import React from 'react';
import {
  IconButton,
  Tooltip,
  Box,
  useTheme as useMuiTheme,
  Zoom,
  Fade
} from '@mui/material';
import {
  LightMode,
  DarkMode
} from '@mui/icons-material';
import { useTheme } from '../../contexts/ThemeContext';

const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const muiTheme = useMuiTheme();
  const isDark = theme === 'dark';

  return (
    <Tooltip
      title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
      TransitionComponent={Zoom}
      arrow
      placement="bottom"
    >
      <IconButton
        onClick={toggleTheme}
        sx={{
          width: 40,
          height: 40,
          backgroundColor: isDark
            ? 'rgba(251, 191, 36, 0.1)'
            : 'rgba(59, 130, 246, 0.1)',
          border: '1px solid',
          borderColor: isDark
            ? 'rgba(251, 191, 36, 0.3)'
            : 'rgba(59, 130, 246, 0.3)',
          color: isDark ? '#fbbf24' : '#3b82f6',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          '&:hover': {
            backgroundColor: isDark
              ? 'rgba(251, 191, 36, 0.2)'
              : 'rgba(59, 130, 246, 0.2)',
            borderColor: isDark
              ? 'rgba(251, 191, 36, 0.5)'
              : 'rgba(59, 130, 246, 0.5)',
            transform: 'scale(1.05)',
            boxShadow: isDark
              ? '0 4px 12px rgba(251, 191, 36, 0.3)'
              : '0 4px 12px rgba(59, 130, 246, 0.3)',
          },
          '&:active': {
            transform: 'scale(0.95)',
          },
        }}
        size="large"
      >
        <Box
          sx={{
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          {/* Light Mode Icon */}
          <Fade in={!isDark} timeout={300}>
            <Box
              sx={{
                position: 'absolute',
                opacity: isDark ? 0 : 1,
                transform: isDark ? 'rotate(-90deg) scale(0.8)' : 'rotate(0deg) scale(1)',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
            >
              <LightMode
                sx={{
                  fontSize: 20,
                  filter: 'drop-shadow(0 2px 4px rgba(59, 130, 246, 0.3))'
                }}
              />
            </Box>
          </Fade>

          {/* Dark Mode Icon */}
          <Fade in={isDark} timeout={300}>
            <Box
              sx={{
                position: 'absolute',
                opacity: isDark ? 1 : 0,
                transform: isDark ? 'rotate(0deg) scale(1)' : 'rotate(90deg) scale(0.8)',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
            >
              <DarkMode
                sx={{
                  fontSize: 20,
                  filter: 'drop-shadow(0 2px 4px rgba(251, 191, 36, 0.3))'
                }}
              />
            </Box>
          </Fade>
        </Box>
      </IconButton>
    </Tooltip>
  );
};

export default ThemeToggle;