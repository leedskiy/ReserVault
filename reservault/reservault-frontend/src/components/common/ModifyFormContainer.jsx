import { useEffect } from "react";
import { motion } from "framer-motion";
import { FaTimes } from "react-icons/fa";
import ImageUploader from "./ImageUploader";

const ModifyFormContainer = ({
    title,
    onClose,
    onSubmit,
    leftContent,
    rightContent,
    images,
    handleDragStart,
    handleDragOver,
    handleDrop,
    handleRemoveImage,
    newImages,
    setNewImages,
    isDragging,
    setIsDragging,
    isDirty,
    setImagesToDelete,
    itemsName = "Item",
}) => {
    useEffect(() => {
        const handleBeforeUnload = (event) => {
            if (isDirty) {
                event.preventDefault();
                event.returnValue = "";
            }
        };

        window.addEventListener("beforeunload", handleBeforeUnload);
        return () => {
            window.removeEventListener("beforeunload", handleBeforeUnload);
        };
    }, [isDirty]);

    const handleClose = () => {
        setImagesToDelete([]);
        onClose();
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <motion.div
                className="flex flex-col justify-between bg-white rounded-lg shadow-lg p-6 w-full max-w-5xl relative"
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, ease: "easeOut" }}
            >
                <button
                    className="duration-200 ml-auto w-8 h-8 flex items-center justify-center rounded-lg text-[#32492D] hover:text-[#273823] hover:bg-gray-200"
                    onClick={handleClose}
                >
                    <FaTimes size={20} />
                </button>

                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    {title}
                </h2>

                <div className="flex gap-10 h-full p-4">
                    {leftContent}

                    <div className="flex flex-col flex-grow max-w-[40%] space-y-6">
                        <div>
                            <label className="block text-gray-600">Manage Images</label>
                            <div className="flex gap-x-4 overflow-x-auto p-2 border rounded-lg whitespace-nowrap">
                                {images?.map((img, index) => (
                                    <div
                                        key={img}
                                        draggable
                                        onDragStart={() => handleDragStart(index)}
                                        onDragOver={handleDragOver}
                                        onDrop={() => handleDrop(index)}
                                        className="relative inline-block cursor-move"
                                    >
                                        {images.length > 1 && (
                                            <button
                                                className="absolute top-6 right-0 bg-red-600 text-white rounded-full p-1"
                                                onClick={() => handleRemoveImage(index)}
                                            >
                                                <FaTimes size={14} />
                                            </button>
                                        )}
                                        <span className="text-sm font-bold mb-1 block text-center">
                                            #{index + 1}
                                        </span>
                                        <img
                                            src={img}
                                            alt={itemsName}
                                            className="w-24 h-24 rounded-lg shadow object-cover min-w-[96px] min-h-[96px]"
                                        />
                                    </div>
                                ))}
                            </div>
                        </div>

                        <ImageUploader
                            images={newImages}
                            setImages={setNewImages}
                            isDragging={isDragging}
                            setIsDragging={setIsDragging}
                            itemsName={itemsName}
                        />
                    </div>

                    {rightContent}
                </div>

                <div className="flex justify-end space-x-4 mt-6">
                    <button
                        type="button"
                        className="px-8 py-2 min-w-32 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300"
                        onClick={handleClose}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="px-8 py-2 min-w-32 text-white bg-[#32492D] hover:bg-[#273823] rounded-lg transition-all duration-300"
                        onClick={onSubmit}
                    >
                        Submit
                    </button>
                </div>
            </motion.div>
        </div>
    );
};

export default ModifyFormContainer;