import React, { useState } from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  Box,
  Typography,
  IconButton,
  Divider,
  Avatar,
} from '@mui/material';
import {
  Home,
  Payment,
  CreditCard,
  People,
  Receipt,
  AccountBalance,
  Webhook,
  Analytics,
  Settings,
  ExpandLess,
  ExpandMore,
  ChevronLeft,
  ChevronRight,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const DRAWER_WIDTH = 280;
const DRAWER_WIDTH_COLLAPSED = 72;

interface SidebarProps {
  open: boolean;
  onToggle: () => void;
}

interface MenuItem {
  id: string;
  title: string;
  icon: React.ReactNode;
  path?: string;
  children?: MenuItem[];
}

const menuItems: MenuItem[] = [
  {
    id: 'process-payment',
    title: 'Process Payment',
    icon: <CreditCard />,
    path: '/dashboard/process-payment'
  },
  {
    id: 'operations',
    title: 'Operations',
    icon: <Payment />,
    children: [
      {
        id: 'payments',
        title: 'Payments',
        icon: <Payment />,
        path: '/dashboard/payments'
      },
      {
        id: 'refunds',
        title: 'Refunds',
        icon: <Receipt />,
        path: '/dashboard/refunds'
      },
      {
        id: 'disputes',
        title: 'Disputes',
        icon: <AccountBalance />,
        path: '/dashboard/disputes'
      }
    ]
  },
  {
    id: 'customers',
    title: 'Customers',
    icon: <People />,
    path: '/dashboard/customers'
  },
  {
    id: 'analytics',
    title: 'Analytics',
    icon: <Analytics />,
    path: '/dashboard/analytics'
  },
  {
    id: 'webhooks',
    title: 'Webhooks',
    icon: <Webhook />,
    path: '/dashboard/webhooks'
  },
  {
    id: 'settings',
    title: 'Settings',
    icon: <Settings />,
    path: '/dashboard/settings'
  }
];

const Sidebar: React.FC<SidebarProps> = ({ open, onToggle }) => {
  const [expandedItems, setExpandedItems] = useState<string[]>(['operations']);
  const location = useLocation();
  const navigate = useNavigate();
  const { state } = useAuth();

  const handleItemClick = (item: MenuItem) => {
    if (item.children) {
      // Toggle expansion for parent items
      setExpandedItems(prev => 
        prev.includes(item.id) 
          ? prev.filter(id => id !== item.id)
          : [...prev, item.id]
      );
    } else if (item.path) {
      // Navigate to path for leaf items
      navigate(item.path);
    }
  };

  const isItemActive = (path?: string) => {
    if (!path) return false;
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  const renderMenuItem = (item: MenuItem, level: number = 0) => {
    const hasChildren = item.children && item.children.length > 0;
    const isExpanded = expandedItems.includes(item.id);
    const isActive = isItemActive(item.path);

    return (
      <React.Fragment key={item.id}>
        <ListItem disablePadding sx={{ display: 'block' }}>
          <ListItemButton
            onClick={() => handleItemClick(item)}
            sx={{
              minHeight: 48,
              justifyContent: open ? 'initial' : 'center',
              px: 2.5,
              pl: level > 0 ? 4 : 2.5,
              backgroundColor: isActive ? 'primary.main' : 'transparent',
              color: isActive ? 'primary.contrastText' : 'text.primary',
              '&:hover': {
                backgroundColor: isActive ? 'primary.dark' : 'action.hover',
              },
            }}
          >
            <ListItemIcon
              sx={{
                minWidth: 0,
                mr: open ? 3 : 'auto',
                justifyContent: 'center',
                color: isActive ? 'primary.contrastText' : 'text.secondary',
              }}
            >
              {item.icon}
            </ListItemIcon>
            <ListItemText 
              primary={item.title} 
              sx={{ opacity: open ? 1 : 0 }}
            />
            {hasChildren && open && (
              isExpanded ? <ExpandLess /> : <ExpandMore />
            )}
          </ListItemButton>
        </ListItem>
        
        {hasChildren && (
          <Collapse in={isExpanded && open} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.children!.map(child => renderMenuItem(child, level + 1))}
            </List>
          </Collapse>
        )}
      </React.Fragment>
    );
  };

  return (
    <Drawer
      variant="permanent"
      open={open}
      sx={{
        width: open ? DRAWER_WIDTH : DRAWER_WIDTH_COLLAPSED,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: open ? DRAWER_WIDTH : DRAWER_WIDTH_COLLAPSED,
          boxSizing: 'border-box',
          transition: theme => theme.transitions.create('width', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
          }),
          overflowX: 'hidden',
        },
      }}
    >
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: 2,
          py: 1,
          minHeight: 64,
        }}
      >
        {open && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
              PG
            </Avatar>
            <Typography variant="h6" noWrap component="div" sx={{ fontWeight: 600 }}>
              Payment Gateway
            </Typography>
          </Box>
        )}
        <IconButton onClick={onToggle} size="small">
          {open ? <ChevronLeft /> : <ChevronRight />}
        </IconButton>
      </Box>

      <Divider />

      {/* Merchant Info */}
      {open && state.user && (
        <Box sx={{ px: 2, py: 1 }}>
          <Typography variant="caption" color="text.secondary">
            Merchant Account
          </Typography>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {state.user.merchantName}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Profile: default
          </Typography>
        </Box>
      )}

      <Divider />

      {/* ORCHESTRATOR Section */}
      {open && (
        <Box sx={{ px: 2, py: 1 }}>
          <Typography 
            variant="overline" 
            sx={{ 
              color: 'text.secondary',
              fontSize: '0.75rem',
              fontWeight: 600,
              letterSpacing: 1
            }}
          >
            ORCHESTRATOR
          </Typography>
        </Box>
      )}

      {/* Menu Items */}
      <List sx={{ flexGrow: 1 }}>
        {menuItems.map(item => renderMenuItem(item))}
      </List>

      {/* User Profile at Bottom */}
      {state.user && (
        <>
          <Divider />
          <Box sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Avatar sx={{ width: 32, height: 32 }}>
                {state.user.email.charAt(0).toUpperCase()}
              </Avatar>
              {open && (
                <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                  <Typography variant="body2" noWrap>
                    {state.user.email}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {state.user.role}
                  </Typography>
                </Box>
              )}
            </Box>
          </Box>
        </>
      )}
    </Drawer>
  );
};

export default Sidebar;
