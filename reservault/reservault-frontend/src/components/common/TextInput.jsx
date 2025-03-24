const TextInput = ({ label, name, value, onChange, type = "text", required = true }) => (
    <div>
        <label className="block text-gray-600">{label}</label>
        <input
            type={type}
            name={name}
            value={value}
            onChange={onChange}
            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
            required={required}
        />
    </div>
);

export default TextInput;