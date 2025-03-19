import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { FcGoogle } from "react-icons/fc";
import { FaTimes } from "react-icons/fa";
import api from "../../api/axios";
import PopupModal from "../../components/common/PopupModal";

const Register = () => {
    const { register, handleSubmit, watch, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const [serverError, setServerError] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [popupMessage, setPopupMessage] = useState("");
    const [isManager, setIsManager] = useState(false);
    const [hotelIdentifier, setHotelIdentifier] = useState("");
    const [hotelIdentifiers, setHotelIdentifiers] = useState([]);

    const mutation = useMutation({
        mutationFn: async (formData) => {
            const response = await api.post("/auth/register", formData);
            return response.data;
        },
        onSuccess: (_, variables) => {
            const message = variables.isManager
                ? "A confirmation email has been sent to your inbox. Please check your email and verify your account. " +
                "After that manager registration request will be submitted and has to be approved by the administration."
                : "A confirmation email has been sent to your inbox. Please check your email and verify your account before logging in.";
            setPopupMessage(message);
            setShowPopup(true);
        },
        onError: (error) => {
            setServerError(error.response?.data?.message || "Registration failed");
        },
    });

    const onSubmit = (data) => {
        setServerError(null);

        const formattedData = {
            ...data,
            isManager,
            hotelIdentifiers: isManager ? hotelIdentifiers : [],
        };
        console.log("Submitting data:", formattedData);
        mutation.mutate(formattedData);
    };

    const handleClosePopup = () => {
        setShowPopup(false);
        navigate("/");
    };

    const handleGoogleSignIn = () => {
        window.location.href = import.meta.env.VITE_API_BASE_URL + "/oauth2/login/google";
    };

    const handleAddHotelIdentifier = () => {
        if (hotelIdentifier.trim() && !hotelIdentifiers.includes(hotelIdentifier.trim())) {
            setHotelIdentifiers([...hotelIdentifiers, hotelIdentifier.trim()]);
            setHotelIdentifier("");
        }
    };

    const handleRemoveHotelIdentifier = (identifier) => {
        setHotelIdentifiers(hotelIdentifiers.filter(id => id !== identifier));
    };

    const handleKeyPress = (e) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            handleAddHotelIdentifier();
        }
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
                            {...register("password", {
                                required: "Password is required",
                                minLength: { value: 6, message: "Password must be at least 6 characters" }
                            })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.password && <p className="text-red-500 text-sm">{errors.password.message}</p>}
                    </div>

                    <div>
                        <label className="block text-gray-600">Confirm Password</label>
                        <input
                            type="password"
                            {...register("confirmPassword", {
                                required: "Please confirm your password",
                                validate: (value) => value === watch("password") || "Passwords do not match"
                            })}
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                        />
                        {errors.confirmPassword && (
                            <p className="text-red-500 text-sm">{errors.confirmPassword.message}</p>
                        )}
                    </div>

                    <div className="flex items-center gap-2">
                        <input
                            type="checkbox"
                            id="isManager"
                            checked={isManager}
                            onChange={() => {
                                setIsManager(!isManager);
                                if (isManager) setHotelIdentifiers([]);
                            }}
                            className="w-5 h-5 border-gray-400 rounded-md appearance-none cursor-pointer duration-300
                                peer checked:bg-[#32492D] checked:border-[#273823] checked:hover:bg-[#273823] border-2 focus:ring-0"
                        />
                        <label htmlFor="isManager" className="text-gray-700 font-semibold">
                            Register as Hotel Manager
                        </label>
                    </div>

                    <div className="mt-2">
                        <label className="block text-gray-600">Hotel Identifiers</label>

                        <div className="flex gap-2">
                            <input
                                type="text"
                                value={hotelIdentifier}
                                onChange={(e) => setHotelIdentifier(e.target.value)}
                                onKeyDown={handleKeyPress}
                                placeholder="Enter hotel identifier"
                                disabled={!isManager}
                                className={`w-full px-4 py-2 border rounded-lg focus:ring duration-300 
                                ${isManager ? "focus:ring-blue-300 border-gray-400" : "bg-gray-300 text-gray-500"}`}
                            />

                            <button
                                type="button"
                                onClick={handleAddHotelIdentifier}
                                disabled={!isManager}
                                className={`px-4 py-2 rounded-lg transition-all duration-300
                                ${isManager ? "bg-[#32492D] hover:bg-[#273823] text-white" : "bg-gray-300 text-gray-500"}`}
                            >
                                Add
                            </button>
                        </div>

                        <div className="mt-2 flex flex-wrap gap-2">
                            {hotelIdentifiers.map((identifier, index) => (
                                <div key={index} className="flex items-center gap-1 bg-gray-200 px-2 py-1 rounded duration-300">
                                    <span>{identifier}</span>
                                    <button
                                        type="button"
                                        className="bg-red-600 text-white p-1 rounded-full duration-300"
                                        onClick={() => handleRemoveHotelIdentifier(identifier)}
                                    >
                                        <FaTimes size={10} />
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>


                    <button
                        type="submit"
                        className="w-full px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                        disabled={mutation.isLoading}
                    >
                        {mutation.isLoading ? "Registering..." : "Register"}
                    </button>
                </form>

                <div className="flex items-center justify-center mt-4">
                    <button
                        onClick={handleGoogleSignIn}
                        className="w-full flex items-center justify-center px-4 py-2 border rounded-lg bg-white shadow-md hover:bg-gray-200 transition-all duration-300"
                    >
                        <FcGoogle className="text-2xl mr-2" />
                        <span className="text-gray-700 font-semibold">Sign up with Google</span>
                    </button>
                </div>

                <p className="mt-4 text-gray-500 text-center">
                    Already have an account?{" "}
                    <Link to="/login" className="text-[#32492D] font-semibold hover:underline">
                        Sign in
                    </Link>
                </p>
            </div>

            {showPopup && (
                <PopupModal message={popupMessage} onClose={handleClosePopup} />
            )}
        </div>
    );
};

export default Register;
