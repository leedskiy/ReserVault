const TextInput = ({ label, name, value, onChange, type = "text", required = true }) => (
    <div>
        <label className="block text-gray-600 font-medium mb-1">{label}</label>
        <input
            type={type}
            name={name}
            value={value}
            onChange={onChange}
            className="w-full border border-gray-300 rounded-md px-3 py-2
                        focus:outline-none transition-all duration-100 ease-in-out transform
                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
            required={required}
        />
    </div>
);

export default TextInput;