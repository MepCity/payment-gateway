import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  TextField,
  MenuItem,
  Chip,
  IconButton,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Add,
  Search,
  FilterList,
  Refresh,
  Download,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { dashboardAPI } from '../services/dashboardApi';
import { RefundListItem, RefundStats, RefundFilters, RefundStatus } from '../types/dashboard';
import RefundsTable from '../components/refunds/RefundsTable';
import StatsCards, { StatsCard } from '../components/common/StatsCards';

const RefundsPage: React.FC = () => {
  const navigate = useNavigate();
  const { state: authState } = useAuth();
  
  const [refunds, setRefunds] = useState<RefundListItem[]>([]);
  const [stats, setStats] = useState<RefundStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);

  const pageSize = 25;

  const loadRefunds = async (page: number = 1) => {
    if (!authState.user?.merchantId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const filters: RefundFilters = {};
      if (searchTerm) filters.search = searchTerm;
      if (statusFilter) filters.status = [statusFilter as RefundStatus];

      const response = await dashboardAPI.getRefunds(
        authState.user.merchantId,
        filters,
        page,
        pageSize
      );
      
      setRefunds(response.refunds);
      setCurrentPage(response.pagination.currentPage || response.pagination.page);
      setTotalPages(response.pagination.totalPages);
      setTotalCount(response.pagination.totalCount);
      
      // Load stats
      const statsData = await dashboardAPI.getRefundStats(authState.user.merchantId);
      setStats(statsData);
      
    } catch (err: any) {
      console.error('Error loading refunds:', err);
      setError(err.message || 'Failed to load refunds');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRefunds();
  }, [authState.user?.merchantId]);

  const handleSearch = () => {
    setCurrentPage(1);
    loadRefunds(1);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    loadRefunds(page);
  };

  const handleRefresh = () => {
    loadRefunds(currentPage);
  };

  const handleViewRefund = (refund: RefundListItem) => {
    navigate(`/dashboard/refunds/${refund.refundId}`);
  };

  const handleProcessRefund = () => {
    navigate('/dashboard/refunds/process');
  };

  const getStatsCards = (): StatsCard[] => {
    if (!stats) return [];
    
    return [
      {
        title: 'Total Refunds',
        value: stats.totalRefunds.toString(),
        subtitle: `${stats.totalRefundAmount.toFixed(2)} total amount`,
        color: 'primary' as const,
      },
      {
        title: 'Completed',
        value: stats.completedRefunds.toString(),
        subtitle: `${((stats.completedRefunds / Math.max(stats.totalRefunds, 1)) * 100).toFixed(1)}% completion rate`,
        color: 'success' as const,
      },
      {
        title: 'Pending',
        value: stats.pendingRefunds.toString(),
        subtitle: 'Awaiting processing',
        color: 'warning' as const,
      },
      {
        title: 'Failed',
        value: stats.failedRefunds.toString(),
        subtitle: 'Require attention',
        color: 'error' as const,
      },
    ];
  };

  if (loading && refunds.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Refunds
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={() => console.log('Export refunds')}
          >
            Export
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={handleProcessRefund}
          >
            Process Refund
          </Button>
        </Box>
      </Box>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Stats Cards */}
      {stats && (
        <Box sx={{ mb: 3 }}>
          <StatsCards cards={getStatsCards()} />
        </Box>
      )}

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ 
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: 'repeat(4, 1fr) auto' },
            gap: 2,
            alignItems: 'center'
          }}>
            <TextField
              placeholder="Search refunds..."
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={handleKeyPress}
              InputProps={{
                startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
            />
            
            <TextField
              select
              label="Status"
              size="small"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <MenuItem value="">All Statuses</MenuItem>
              {Object.values(RefundStatus).map((status) => (
                <MenuItem key={status} value={status}>
                  {status.replace('_', ' ')}
                </MenuItem>
              ))}
            </TextField>

            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                onClick={handleSearch}
                startIcon={<Search />}
              >
                Search
              </Button>
              
              <IconButton onClick={handleRefresh} disabled={loading}>
                <Refresh />
              </IconButton>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Refunds Table */}
      <RefundsTable
        refunds={refunds}
        loading={loading}
        currentPage={currentPage}
        totalPages={totalPages}
        totalCount={totalCount}
        pageSize={pageSize}
        onPageChange={handlePageChange}
        onViewRefund={handleViewRefund}
      />
    </Box>
  );
};

export default RefundsPage;