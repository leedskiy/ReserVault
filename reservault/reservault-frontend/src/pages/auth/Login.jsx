import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useEffect, useState } from "react";
import { FcGoogle } from "react-icons/fc";

const Login = () => {
    const { login } = useAuth();
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm();

    const [serverError, setServerError] = useState(sessionStorage.getItem("loginError") || "");

    useEffect(() => {
        const handleBeforeUnload = () => {
            sessionStorage.removeItem("loginError");
        };

        window.addEventListener("beforeunload", handleBeforeUnload);
        return () => {
            window.removeEventListener("beforeunload", handleBeforeUnload);
        };
    }, []);

    const onSubmit = async (credentials) => {
        setServerError("");
        try {
            await login(credentials);
            sessionStorage.removeItem("loginError");
            navigate("/dashboard");
        } catch (errorMessage) {
            setServerError(errorMessage);
            sessionStorage.setItem("loginError", errorMessage);
            setError("email", { message: " " });
            setError("password", { message: " " });
        }
    };

    const handleGoogleSignIn = () => {
        window.location.href = import.meta.env.VITE_API_BASE_URL + "/oauth2/login/google";
    };

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Sign In
                </h2>

                {serverError && <p className="text-red-500 text-center mb-3">{serverError}</p>}

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div>
                        <label className="block text-gray-600">Email</label>
                        <input
                            type="email"
                            {...register("email", { required: "Email is required" })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.email && <p className="text-red-500 text-sm">{errors.email.message}</p>}
                    </div>

                    <div>
                        <label className="block text-gray-600">Password</label>
                        <input
                            type="password"
                            {...register("password", { required: "Password is required" })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.password && <p className="text-red-500 text-sm">{errors.password.message}</p>}
                    </div>

                    <button
                        type="submit"
                        className="w-full px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Logging in..." : "Login"}
                    </button>
                </form>

                <div className="flex items-center justify-center mt-4">
                    <button
                        onClick={handleGoogleSignIn}
                        className="w-full flex items-center justify-center px-4 py-2 border rounded-lg bg-white shadow-md hover:bg-gray-200 transition-all duration-300"
                    >
                        <FcGoogle className="text-2xl mr-2" />
                        <span className="text-gray-700 font-semibold">Sign in with Google</span>
                    </button>
                </div>

                <p className="mt-4 text-gray-500 text-center">
                    Don't have an account?{" "}
                    <Link to="/register" className="text-[#32492D] font-semibold hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer">
                        Sign up
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default Login;
