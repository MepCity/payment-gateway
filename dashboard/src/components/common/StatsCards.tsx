import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  useTheme,
} from '@mui/material';

export interface StatsCard {
  title: string;
  value: string | number;
  subtitle: string;
  color: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
}

interface StatsCardsProps {
  cards: StatsCard[];
}

const StatsCards: React.FC<StatsCardsProps> = ({ cards }) => {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  
  const getColorStyles = (color: StatsCard['color']) => {
    const colorMap = {
      primary: { 
        bg: isDark ? 'rgba(96, 165, 250, 0.15)' : 'rgba(25, 118, 210, 0.1)', 
        text: isDark ? '#60a5fa' : '#1976d2' 
      },
      secondary: { 
        bg: isDark ? 'rgba(147, 197, 253, 0.15)' : 'rgba(156, 39, 176, 0.1)', 
        text: isDark ? '#93c5fd' : '#9c27b0' 
      },
      success: { 
        bg: isDark ? 'rgba(52, 211, 153, 0.15)' : 'rgba(46, 125, 50, 0.1)', 
        text: isDark ? '#34d399' : '#2e7d32' 
      },
      warning: { 
        bg: isDark ? 'rgba(251, 191, 36, 0.15)' : 'rgba(237, 108, 2, 0.1)', 
        text: isDark ? '#fbbf24' : '#ed6c02' 
      },
      error: { 
        bg: isDark ? 'rgba(248, 113, 113, 0.15)' : 'rgba(211, 47, 47, 0.1)', 
        text: isDark ? '#f87171' : '#d32f2f' 
      },
      info: { 
        bg: isDark ? 'rgba(59, 130, 246, 0.15)' : 'rgba(2, 136, 209, 0.1)', 
        text: isDark ? '#3b82f6' : '#0288d1' 
      },
    };
    return colorMap[color];
  };

  return (
    <Box sx={{ 
      display: 'grid',
      gridTemplateColumns: { 
        xs: '1fr',
        sm: 'repeat(2, 1fr)',
        md: 'repeat(4, 1fr)'
      },
      gap: 2
    }}>
      {cards.map((card, index) => {
        const colorStyle = getColorStyles(card.color);
        return (
          <Card 
            key={index} 
            sx={{ 
              background: isDark ? 'rgba(30, 41, 59, 0.8)' : colorStyle.bg,
              border: `1px solid ${colorStyle.text}30`,
              backdropFilter: isDark ? 'blur(10px)' : 'none',
            }}
          >
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                {card.title}
              </Typography>
              <Typography 
                variant="h4" 
                sx={{ 
                  fontWeight: 700,
                  color: colorStyle.text,
                  mb: 1
                }}
              >
                {card.value}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {card.subtitle}
              </Typography>
            </CardContent>
          </Card>
        );
      })}
    </Box>
  );
};

export default StatsCards;