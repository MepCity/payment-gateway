import React, { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  OutlinedInput,
  Button,
  Paper,
  Typography,
  Collapse,
  IconButton,
  Grid,
} from '@mui/material';
import {
  Search,
  FilterList,
  Clear,
  ExpandMore,
  ExpandLess,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { DashboardFilters, PaymentStatus, PaymentMethod } from '../../types/dashboard';

interface PaymentsFiltersProps {
  filters: DashboardFilters;
  onFiltersChange: (filters: DashboardFilters) => void;
  onSearch: () => void;
  onClearFilters: () => void;
}

const PaymentsFilters: React.FC<PaymentsFiltersProps> = ({
  filters,
  onFiltersChange,
  onSearch,
  onClearFilters,
}) => {
  const [expanded, setExpanded] = useState(false);

  const handleSearchChange = (value: string) => {
    onFiltersChange({
      ...filters,
      search: value || undefined,
    });
  };

  const handleStatusChange = (statuses: PaymentStatus[]) => {
    onFiltersChange({
      ...filters,
      status: statuses.length > 0 ? statuses : undefined,
    });
  };

  const handlePaymentMethodChange = (methods: PaymentMethod[]) => {
    onFiltersChange({
      ...filters,
      paymentMethod: methods.length > 0 ? methods : undefined,
    });
  };

  const handleDateRangeChange = (field: 'startDate' | 'endDate', value: Date | null) => {
    if (!value) {
      // If clearing date, remove entire date range
      if (!filters.dateRange) return;
      
      const newDateRange = { ...filters.dateRange };
      if (field === 'startDate') {
        delete (newDateRange as any).startDate;
      } else {
        delete (newDateRange as any).endDate;
      }
      
      onFiltersChange({
        ...filters,
        dateRange: Object.keys(newDateRange).length > 0 ? newDateRange : undefined,
      });
      return;
    }

    const currentDateRange = filters.dateRange || {};
    onFiltersChange({
      ...filters,
      dateRange: {
        ...currentDateRange,
        [field]: value.toISOString().split('T')[0],
      },
    });
  };

  const hasActiveFilters = () => {
    return !!(
      filters.search ||
      filters.status?.length ||
      filters.paymentMethod?.length ||
      filters.dateRange ||
      filters.amountRange
    );
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Paper sx={{ 
        p: 2, 
        mb: 3,
        backgroundColor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
      }}>
        {/* Search Bar */}
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
          <TextField
            placeholder="Search for payment ID"
            value={filters.search || ''}
            onChange={(e) => handleSearchChange(e.target.value)}
            InputProps={{
              startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
            }}
            sx={{ flex: 1 }}
            size="small"
          />
          
          <Button
            variant="outlined"
            startIcon={<FilterList />}
            endIcon={expanded ? <ExpandLess /> : <ExpandMore />}
            onClick={() => setExpanded(!expanded)}
            sx={{ minWidth: 120 }}
          >
            Add Filters
          </Button>

          {hasActiveFilters() && (
            <Button
              variant="outlined"
              startIcon={<Clear />}
              onClick={onClearFilters}
              color="error"
            >
              Clear
            </Button>
          )}
        </Box>

        {/* Advanced Filters */}
        <Collapse in={expanded}>
          <Box sx={{ pt: 2, borderTop: 1, borderColor: 'divider' }}>
            <Box sx={{ 
              display: 'grid',
              gridTemplateColumns: { 
                xs: '1fr', 
                sm: 'repeat(2, 1fr)', 
                md: 'repeat(4, 1fr)' 
              },
              gap: 2 
            }}>
              {/* Date Range */}
              <Box>
                <DatePicker
                  label="Start Date"
                  value={filters.dateRange?.startDate ? new Date(filters.dateRange.startDate) : null}
                  onChange={(value) => handleDateRangeChange('startDate', value)}
                  slotProps={{
                    textField: { size: 'small', fullWidth: true }
                  }}
                />
              </Box>
              
              <Box>
                <DatePicker
                  label="End Date"
                  value={filters.dateRange?.endDate ? new Date(filters.dateRange.endDate) : null}
                  onChange={(value) => handleDateRangeChange('endDate', value)}
                  slotProps={{
                    textField: { size: 'small', fullWidth: true }
                  }}
                />
              </Box>

              {/* Status Filter */}
              <Box>
                <FormControl fullWidth size="small">
                  <InputLabel>Payment Status</InputLabel>
                  <Select
                    multiple
                    value={filters.status || []}
                    onChange={(e) => handleStatusChange(e.target.value as PaymentStatus[])}
                    input={<OutlinedInput label="Payment Status" />}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {selected.map((value) => (
                          <Chip key={value} label={value} size="small" />
                        ))}
                      </Box>
                    )}
                  >
                    {Object.values(PaymentStatus).map((status) => (
                      <MenuItem key={status} value={status}>
                        {status}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>

              {/* Payment Method Filter */}
              <Box>
                <FormControl fullWidth size="small">
                  <InputLabel>Payment Method</InputLabel>
                  <Select
                    multiple
                    value={filters.paymentMethod || []}
                    onChange={(e) => handlePaymentMethodChange(e.target.value as PaymentMethod[])}
                    input={<OutlinedInput label="Payment Method" />}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {selected.map((value) => (
                          <Chip key={value} label={value.replace('_', ' ')} size="small" />
                        ))}
                      </Box>
                    )}
                  >
                    {Object.values(PaymentMethod).map((method) => (
                      <MenuItem key={method} value={method}>
                        {method.replace('_', ' ')}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>
            </Box>

            <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
              <Button onClick={onClearFilters} disabled={!hasActiveFilters()}>
                Clear Filters
              </Button>
              <Button variant="contained" onClick={onSearch}>
                Apply Filters
              </Button>
            </Box>
          </Box>
        </Collapse>

        {/* Active Filters Display */}
        {hasActiveFilters() && (
          <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 1, alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Active filters:
            </Typography>
            
            {filters.search && (
              <Chip
                label={`Search: ${filters.search}`}
                onDelete={() => handleSearchChange('')}
                size="small"
                variant="outlined"
              />
            )}
            
            {filters.status?.map((status) => (
              <Chip
                key={status}
                label={`Status: ${status}`}
                onDelete={() => handleStatusChange(filters.status!.filter(s => s !== status))}
                size="small"
                variant="outlined"
              />
            ))}
            
            {filters.paymentMethod?.map((method) => (
              <Chip
                key={method}
                label={`Method: ${method.replace('_', ' ')}`}
                onDelete={() => handlePaymentMethodChange(filters.paymentMethod!.filter(m => m !== method))}
                size="small"
                variant="outlined"
              />
            ))}
            
            {filters.dateRange && (
              <Chip
                label={`Date: ${filters.dateRange.startDate || ''} - ${filters.dateRange.endDate || ''}`}
                onDelete={() => onFiltersChange({ ...filters, dateRange: undefined })}
                size="small"
                variant="outlined"
              />
            )}
          </Box>
        )}
      </Paper>
    </LocalizationProvider>
  );
};

export default PaymentsFilters;