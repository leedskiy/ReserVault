import { FaExclamationTriangle } from "react-icons/fa";
import { motion, AnimatePresence } from "framer-motion";

const ConfirmationPopup = ({
    isOpen,
    title,
    message,
    icon: Icon = FaExclamationTriangle,
    onConfirm,
    onCancel,
    confirmLabel = "Confirm",
    cancelLabel = "Cancel",
}) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.3, ease: "easeOut" }}
                >
                    <motion.div
                        className="bg-white p-6 rounded-lg shadow-lg w-full max-w-sm text-center"
                        initial={{ opacity: 0, y: 50 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.4, ease: "easeOut" }}
                    >
                        <div className="flex flex-col items-center space-y-4">
                            <Icon className="text-[#32492D] mx-auto text-5xl" />

                            <h2 className="text-lg font-semibold text-gray-900">{title}</h2>

                            <p className="text-gray-600">{message}</p>

                            <div className="flex justify-center gap-4 mt-4">
                                <button
                                    onClick={onCancel}
                                    className="px-8 py-2 min-w-32 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300"
                                >
                                    {cancelLabel}
                                </button>
                                <button
                                    onClick={onConfirm}
                                    className="px-8 py-2 min-w-32 text-white bg-[#32492D] hover:bg-[#273823] rounded-lg transition-all duration-300"
                                >
                                    {confirmLabel}
                                </button>
                            </div>
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default ConfirmationPopup;