import { useEffect } from "react";
import { FaEnvelope } from "react-icons/fa";
import { motion } from "framer-motion";

const PopupModal = ({
    message,
    onClose,
    title = "Check Your Email",
    icon = <FaEnvelope className="text-[#32492D] mx-auto text-5xl mb-3" />,
}) => {
    useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.key === "Escape") {
                onClose();
            }
        };

        window.addEventListener("keydown", handleKeyDown);
        return () => {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [onClose]);

    return (
        <motion.div
            className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3, ease: "easeOut" }}
        >
            <motion.div
                className="bg-white p-6 rounded-lg shadow-lg w-full max-w-sm text-center"
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 50 }}
                transition={{ duration: 0.4, ease: "easeOut" }}
            >
                {icon}
                <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
                <p className="text-gray-600 mt-2">{message}</p>

                <button
                    onClick={onClose}
                    className="mt-4 px-6 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
                >
                    OK
                </button>
            </motion.div>
        </motion.div>
    );
};

export default PopupModal;