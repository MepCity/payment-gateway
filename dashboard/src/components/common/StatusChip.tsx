import React from 'react';
import { Chip } from '@mui/material';
import { PaymentStatus } from '../../types/dashboard';

interface StatusChipProps {
  status: PaymentStatus;
  size?: 'small' | 'medium';
}

const StatusChip: React.FC<StatusChipProps> = ({ status, size = 'small' }) => {
  const getStatusColor = (status: PaymentStatus) => {
    switch (status) {
      case PaymentStatus.COMPLETED:
        return { 
          color: 'success',
          label: 'Completed'
        };
      case PaymentStatus.PENDING:
        return { 
          color: 'warning',
          label: 'Pending'
        };
      case PaymentStatus.FAILED:
        return { 
          color: 'error',
          label: 'Failed'
        };
      case PaymentStatus.PROCESSING:
        return { 
          color: 'info',
          label: 'Processing'
        };
      case PaymentStatus.CANCELLED:
        return { 
          color: 'default',
          label: 'Cancelled'
        };
      case PaymentStatus.REFUNDED:
        return { 
          color: 'secondary',
          label: 'Refunded'
        };
      default:
        return { 
          color: 'default',
          label: status
        };
    }
  };

  const { color, label } = getStatusColor(status);

  return (
    <Chip
      label={label}
      color={color as any}
      size={size}
      variant="filled"
      sx={{
        fontWeight: 500,
        textTransform: 'uppercase',
        fontSize: '0.75rem',
      }}
    />
  );
};

export default StatusChip;
