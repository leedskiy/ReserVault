import { motion } from "framer-motion";
import { Link } from "react-router-dom";

const Sidebar = ({ options, activeView, basePath }) => {
    return (
        <motion.aside
            className="w-72 bg-white shadow-lg h-full p-4 rounded-md flex-shrink-0 sticky top-6"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: "easeOut" }}
        >
            <motion.nav
                className="space-y-3"
                initial="hidden"
                animate="visible"
                variants={{
                    hidden: { opacity: 0 },
                    visible: {
                        opacity: 1,
                        transition: { staggerChildren: 0.15 },
                    },
                }}
            >
                {options.map((option, index) => (
                    <Link key={option.value} to={`/${basePath}/${option.value}`} className="block">
                        <motion.button
                            className={`flex items-center gap-3 w-full text-left py-3 px-3 rounded-md font-semibold text-sm duration-200 transition ${activeView === option.value
                                ? "bg-gray-300 text-gray-900 shadow-md"
                                : "text-gray-600 hover:bg-gray-200"
                                }`}
                            initial={{ opacity: 0, y: 10 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: index * 0.1, duration: 0.3, ease: "easeOut" }}
                        >
                            {option.icon && <option.icon className="w-5 h-5 text-[#32492D]" />}
                            {option.label}
                        </motion.button>
                    </Link>
                ))}
            </motion.nav>
        </motion.aside>
    );
};

export default Sidebar;
