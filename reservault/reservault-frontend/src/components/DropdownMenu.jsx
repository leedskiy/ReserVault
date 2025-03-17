import { motion, AnimatePresence } from "framer-motion";

const DropdownMenu = ({ isOpen, onClose, menuItems, position = "right-0" }) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    className={`absolute ${position} bg-white border rounded-lg shadow-lg z-50 w-48`}
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    transition={{ duration: 0.3, ease: "easeOut" }}
                >
                    {menuItems.map((item, index) => {
                        const isFirst = index === 0;
                        const isLast = index === menuItems.length - 1;

                        return (
                            <button
                                key={index}
                                onClick={() => {
                                    item.onClick();
                                    onClose();
                                }}
                                className={`w-full flex items-center px-4 py-2 text-[#32492D] hover:bg-gray-200 active:bg-gray-300 transition-all duration-200 ease-in-out 
                                    ${isFirst ? "rounded-t-lg" : ""} 
                                    ${isLast ? "rounded-b-lg" : ""}`}
                            >
                                <item.icon className="mr-2" />
                                {item.label}
                            </button>
                        );
                    })}
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default DropdownMenu;
