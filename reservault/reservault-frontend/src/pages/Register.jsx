import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useState } from "react";

const Register = () => {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const [serverError, setServerError] = useState(null);

    const mutation = useMutation({
        mutationFn: async (formData) => {
            const response = await api.post("/auth/register", formData);
            return response.data;
        },
        onSuccess: () => {
            navigate("/login");
        },
        onError: (error) => {
            setServerError(error.response?.data?.message || "Registration failed");
        },
    });

    const onSubmit = (data) => {
        setServerError(null);
        mutation.mutate(data);
    };

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Create an Account
                </h2>
                {serverError && <p className="text-red-500 text-center mb-3">{serverError}</p>}

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div>
                        <label className="block text-gray-600">Full Name</label>
                        <input
                            type="text"
                            {...register("name", { required: "Name is required" })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.name && <p className="text-red-500 text-sm">{errors.name.message}</p>}
                    </div>

                    <div>
                        <label className="block text-gray-600">Email</label>
                        <input
                            type="email"
                            {...register("email", {
                                required: "Email is required",
                                pattern: { value: /^\S+@\S+$/i, message: "Invalid email address" }
                            })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.email && <p className="text-red-500 text-sm">{errors.email.message}</p>}
                    </div>

                    <div>
                        <label className="block text-gray-600">Password</label>
                        <input
                            type="password"
                            {...register("password", { required: "Password is required", minLength: { value: 6, message: "Password must be at least 6 characters" } })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.password && <p className="text-red-500 text-sm">{errors.password.message}</p>}
                    </div>

                    <button
                        type="submit"
                        className="w-full px-4 py-2 text-white bg-gray-900 rounded-lg hover:bg-[#32492D] transition-all duration-300 ease-in-out transform"
                        disabled={mutation.isLoading}
                    >
                        {mutation.isLoading ? "Registering..." : "Register"}
                    </button>
                </form>

                <p className="mt-4 text-gray-500 text-center">
                    Already have an account?{" "}
                    <Link to="/login" className="text-blue-600 hover:underline">
                        Sign in
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default Register;
