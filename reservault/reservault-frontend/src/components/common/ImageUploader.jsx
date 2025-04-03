import { FaTimes } from "react-icons/fa";
import { useRef } from "react";

const ImageUploader = ({ images = [], setImages, isDragging, setIsDragging, itemsName }) => {
    const fileInputRef = useRef(null);

    const validateFiles = (files) => {
        const sizeLimit = 1.5 * 1024 * 1024; // 1.5mb
        const validFormats = ['image/png', 'image/jpeg'];

        const invalidFiles = files.filter(file => !validFormats.includes(file.type) || file.size > sizeLimit);

        return invalidFiles;
    };

    const handleFileChange = (e) => {
        const files = Array.from(e.target.files);
        const invalidFiles = validateFiles(files);

        if (invalidFiles.length > 0) {
            alert("Please upload only PNG or JPG images that are less than 1.5 MB.");
        } else {
            setImages((prev) => [...(Array.isArray(prev) ? prev : []), ...files]);
        }

        e.target.value = "";
    };

    const removeImage = (index, event) => {
        event.stopPropagation();
        setImages((prev) => prev.filter((_, i) => i !== index));
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleDragLeave = (e) => {
        e.preventDefault();
        setIsDragging(false);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setIsDragging(false);
        const files = Array.from(e.dataTransfer.files);
        const invalidFiles = validateFiles(files);

        if (invalidFiles.length > 0) {
            alert("Please upload only PNG or JPG images that are less than 1.5 MB.");
        } else {
            setImages((prev) => [...(Array.isArray(prev) ? prev : []), ...files]);
        }
    };

    return (
        <div>
            <label className="block text-gray-600 font-medium mb-1">Add Images</label>
            <div
                className={`cursor-pointer border-dashed border-2 p-2 flex items-center overflow-x-auto rounded-lg gap-x-4 relative min-h-[120px]
                    ${isDragging ? "border-[#32492D] bg-[#c3cfc0]" : "border-gray-300 bg-gray-100"}`}
                onDragOver={handleDragOver}
                onDragEnter={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current.click()}
            >
                <input
                    type="file"
                    multiple
                    accept="image/*"
                    onChange={handleFileChange}
                    className="hidden"
                    id="fileUpload"
                    ref={fileInputRef}
                />

                {images.length === 0 && (
                    <p className="text-gray-600 font-medium text-center mx-auto">
                        Upload {itemsName} Images
                    </p>
                )}

                {images.map((file, index) => (
                    <div key={index} className="relative inline-block">
                        <button
                            type="button"
                            className="absolute top-0 right-0 bg-red-600 text-white rounded-full p-1"
                            onClick={(e) => removeImage(index, e)}
                        >
                            <FaTimes size={14} />
                        </button>

                        <img
                            src={file instanceof File ? URL.createObjectURL(file) : file}
                            className="w-24 h-24 rounded-lg shadow object-cover min-w-[96px] min-h-[96px]"
                            alt={`${itemsName} Preview`}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ImageUploader;