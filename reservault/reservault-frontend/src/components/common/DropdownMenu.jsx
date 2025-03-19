import { motion, AnimatePresence } from "framer-motion";
import { useState } from "react";

const DropdownMenu = ({ isOpen, onClose, menuItems, position = "right-0" }) => {
    const [isAnimating] = useState(false);

    const dropdownVariants = {
        hidden: { opacity: 0, y: -10 },
        visible: { opacity: 1, y: 0 },
        exit: { opacity: 0, y: -10, transition: { duration: 0.2 } },
    };

    return (
        <AnimatePresence>
            {(isOpen || isAnimating) && (
                <motion.div
                    className={`absolute ${position} bg-white border rounded-lg shadow-lg z-50 w-48`}
                    variants={dropdownVariants}
                    initial="hidden"
                    animate="visible"
                    exit="exit"
                    onAnimationComplete={() => !isOpen && onClose()}
                    onClick={(e) => e.stopPropagation()}
                >
                    {menuItems.map((item, index) => {
                        const isFirst = index === 0;
                        const isLast = index === menuItems.length - 1;

                        return (
                            <button
                                key={index}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    item.onClick();
                                    onClose();
                                }}
                                className={`w-full flex items-center px-4 py-2 text-[#32492D] hover:bg-gray-200 active:bg-gray-300 transition-all duration-200 ease-in-out 
                                    ${isFirst ? "rounded-t-lg" : ""} 
                                    ${isLast ? "rounded-b-lg" : ""}`
                                }
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
