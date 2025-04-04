import { motion } from "framer-motion";

const AddFormContainer = ({ title, errorMessage, onSubmit, children }) => (
    <motion.div
        className="flex items-center justify-center flex-col"
        initial={{ opacity: 0, y: 50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: "easeOut" }}
    >
        <div className="bg-white p-8 rounded-lg shadow-lg w-full">
            <h2 className="text-2xl font-semibold text-gray-900 text-center">{title}</h2>
            {errorMessage && <div className="text-center mb-2 mt-2 text-red-500">{errorMessage}</div>}
            <form onSubmit={onSubmit} className="space-y-4 w-3/4 mx-auto">{children}</form>
        </div>
    </motion.div>
);

export default AddFormContainer;