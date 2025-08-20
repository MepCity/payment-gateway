import React from 'react';
import { Chip } from '@mui/material';
import { PaymentStatus, RefundStatus, CustomerStatus } from '../../types/dashboard';

interface StatusChipProps {
  status: PaymentStatus | RefundStatus | CustomerStatus;
  size?: 'small' | 'medium';
}

const StatusChip: React.FC<StatusChipProps> = ({ status, size = 'small' }) => {
  const getStatusColor = (status: PaymentStatus | RefundStatus | CustomerStatus) => {
    switch (status) {
      case PaymentStatus.COMPLETED:
      case RefundStatus.COMPLETED:
        return { 
          color: 'success',
          label: 'Completed'
        };
      case PaymentStatus.PENDING:
      case RefundStatus.PENDING:
        return { 
          color: 'warning',
          label: 'Pending'
        };
      case PaymentStatus.FAILED:
      case RefundStatus.FAILED:
        return { 
          color: 'error',
          label: 'Failed'
        };
      case PaymentStatus.PROCESSING:
      case RefundStatus.PROCESSING:
        return { 
          color: 'info',
          label: 'Processing'
        };
      case PaymentStatus.CANCELLED:
      case RefundStatus.CANCELLED:
        return { 
          color: 'default',
          label: 'Cancelled'
        };
      case PaymentStatus.REFUNDED:
        return { 
          color: 'secondary',
          label: 'Refunded'
        };
      
      // Customer Status
      case CustomerStatus.ACTIVE:
        return { 
          color: 'success',
          label: 'Active'
        };
      case CustomerStatus.INACTIVE:
        return { 
          color: 'default',
          label: 'Inactive'
        };
      case CustomerStatus.SUSPENDED:
        return { 
          color: 'error',
          label: 'Suspended'
        };
      case CustomerStatus.VERIFIED:
        return { 
          color: 'success',
          label: 'Verified'
        };
      case CustomerStatus.UNVERIFIED:
        return { 
          color: 'warning',
          label: 'Unverified'
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