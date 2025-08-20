import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
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
  const getColorStyles = (color: StatsCard['color']) => {
    const colorMap = {
      primary: { bg: 'rgba(25, 118, 210, 0.1)', text: '#1976d2' },
      secondary: { bg: 'rgba(156, 39, 176, 0.1)', text: '#9c27b0' },
      success: { bg: 'rgba(46, 125, 50, 0.1)', text: '#2e7d32' },
      warning: { bg: 'rgba(237, 108, 2, 0.1)', text: '#ed6c02' },
      error: { bg: 'rgba(211, 47, 47, 0.1)', text: '#d32f2f' },
      info: { bg: 'rgba(2, 136, 209, 0.1)', text: '#0288d1' },
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
              background: colorStyle.bg,
              border: `1px solid ${colorStyle.text}30`
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
