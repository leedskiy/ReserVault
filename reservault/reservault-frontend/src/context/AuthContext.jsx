import { createContext, useContext } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const queryClient = useQueryClient();

    const fetchUserProfile = async () => {
        try {
            const { data } = await api.get('/auth/me');
            return data;
        } catch {
            return null;
        }
    };

    const { data: user, isLoading: loading } = useQuery({
        queryKey: ['auth', 'user'],
        queryFn: fetchUserProfile,
        retry: false,
        refetchOnWindowFocus: false,
    });

    const isAuthenticated = !!user;
    const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;

    const loginMutation = useMutation({
        mutationFn: async (credentials) => {
            await api.post('/auth/login', credentials);
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['auth', 'user'] }),
        onError: (error) => {
            throw error.response?.data?.message || 'Login failed';
        },
    });

    const logoutMutation = useMutation({
        mutationFn: async () => {
            await api.post('/auth/logout');
        },
        onSuccess: () => {
            queryClient.clear();
        },
    });

    const login = (credentials) => loginMutation.mutateAsync(credentials);
    const logout = async (navigate) => {
        await logoutMutation.mutateAsync();
        navigate('/');
    };

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated,
                isAdmin,
                loading,
                login,
                logout,
                refetchUser: () => queryClient.invalidateQueries({ queryKey: ['auth', 'user'] }),
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);