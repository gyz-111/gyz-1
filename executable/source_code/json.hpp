#pragma once
#include <string>
#include <vector>
#include <map>
#include <sstream>
#include <iostream>
#include <cctype>
#include <stdexcept>
#include <cmath>

namespace nlohmann
{

class json
{
public:
    enum value_type { null_t, object_t, array_t, string_t, boolean_t, number_integer_t, number_float_t };

private:
    value_type m_type;
    std::string m_str;
    double m_num;
    bool m_bool;
    std::vector<json> m_array;
    std::map<std::string, json> m_object;

public:
    json() : m_type(null_t) {}
    json(std::nullptr_t) : m_type(null_t) {}
    json(bool val) : m_type(boolean_t), m_bool(val) {}
    json(int val) : m_type(number_integer_t), m_num(val) {}
    json(long val) : m_type(number_integer_t), m_num(val) {}
    json(long long val) : m_type(number_integer_t), m_num(val) {}
    json(unsigned int val) : m_type(number_integer_t), m_num(val) {}
    json(unsigned long val) : m_type(number_integer_t), m_num(val) {}
    json(unsigned long long val) : m_type(number_integer_t), m_num(val) {}
    json(double val) : m_type(number_float_t), m_num(val) {}
    json(const char* val) : m_type(string_t), m_str(val) {}
    json(const std::string& val) : m_type(string_t), m_str(val) {}

    ~json() {}

    json(const json& other) { copy_from(other); }
    json& operator=(const json& other) { if (this != &other) copy_from(other); return *this; }

    json(json&& other) noexcept : m_type(other.m_type), m_str(std::move(other.m_str)), m_num(other.m_num), m_bool(other.m_bool), m_array(std::move(other.m_array)), m_object(std::move(other.m_object)) { other.m_type = null_t; }
    json& operator=(json&& other) noexcept { if (this != &other) { m_type = other.m_type; m_str = std::move(other.m_str); m_num = other.m_num; m_bool = other.m_bool; m_array = std::move(other.m_array); m_object = std::move(other.m_object); other.m_type = null_t; } return *this; }

    static json make_array() { json j; j.m_type = array_t; return j; }
    static json make_object() { json j; j.m_type = object_t; return j; }

    bool is_null() const { return m_type == null_t; }
    bool is_object() const { return m_type == object_t; }
    bool is_array() const { return m_type == array_t; }
    bool is_string() const { return m_type == string_t; }
    bool is_boolean() const { return m_type == boolean_t; }
    bool is_number() const { return m_type == number_integer_t || m_type == number_float_t; }
    bool is_number_integer() const { return m_type == number_integer_t; }
    bool is_number_float() const { return m_type == number_float_t; }

    size_t size() const
    {
        if (is_array()) return m_array.size();
        if (is_object()) return m_object.size();
        return 0;
    }

    bool empty() const { return size() == 0; }

    void push_back(const json& val) { if (!is_array()) { m_type = array_t; m_array.clear(); } m_array.push_back(val); }
    void push_back(json&& val) { if (!is_array()) { m_type = array_t; m_array.clear(); } m_array.push_back(std::move(val)); }

    json& operator[](size_t index)
    {
        if (!is_array()) { m_type = array_t; m_array.clear(); }
        if (index >= m_array.size()) m_array.resize(index + 1);
        return m_array[index];
    }

    const json& operator[](size_t index) const
    {
        if (!is_array()) throw std::out_of_range("not an array");
        if (index >= m_array.size()) throw std::out_of_range("index out of range");
        return m_array[index];
    }

    json& operator[](const std::string& key)
    {
        if (!is_object()) { m_type = object_t; m_object.clear(); }
        return m_object[key];
    }

    const json& operator[](const std::string& key) const
    {
        if (!is_object()) throw std::out_of_range("not an object");
        auto it = m_object.find(key);
        if (it == m_object.end()) throw std::out_of_range("key not found");
        return it->second;
    }

    bool contains(const std::string& key) const
    {
        if (!is_object()) return false;
        return m_object.find(key) != m_object.end();
    }

    template<typename T> T get() const;

    friend std::ostream& operator<<(std::ostream& os, const json& j) { j.serialize(os); return os; }

    static json parse(const std::string& s) { size_t pos = 0; return parse_internal(s, pos); }

private:
    void copy_from(const json& other)
    {
        m_type = other.m_type;
        m_str = other.m_str;
        m_num = other.m_num;
        m_bool = other.m_bool;
        m_array = other.m_array;
        m_object = other.m_object;
    }

    void serialize(std::ostream& os) const
    {
        switch (m_type)
        {
            case null_t: os << "null"; break;
            case object_t:
                os << "{";
                for (auto it = m_object.begin(); it != m_object.end(); )
                {
                    os << "\"" << it->first << "\":" << it->second;
                    if (++it != m_object.end()) os << ",";
                }
                os << "}";
                break;
            case array_t:
                os << "[";
                for (auto it = m_array.begin(); it != m_array.end(); )
                {
                    os << *it;
                    if (++it != m_array.end()) os << ",";
                }
                os << "]";
                break;
            case string_t: os << "\"" << escape(m_str) << "\""; break;
            case boolean_t: os << (m_bool ? "true" : "false"); break;
            case number_integer_t: os << (long long)m_num; break;
            case number_float_t: os << m_num; break;
        }
    }

    std::string escape(const std::string& s) const
    {
        std::string res;
        for (char c : s)
        {
            switch (c) { case '"': res += "\\\""; break; case '\\': res += "\\\\"; break; case '\n': res += "\\n"; break; case '\r': res += "\\r"; break; case '\t': res += "\\t"; break; default: res += c; }
        }
        return res;
    }

    static json parse_internal(const std::string& s, size_t& pos)
    {
        skip_ws(s, pos);
        if (pos >= s.size()) throw std::invalid_argument("unexpected end");
        char c = s[pos];
        if (c == 'n') { expect(s, pos, "null"); return json(); }
        if (c == 't') { expect(s, pos, "true"); return json(true); }
        if (c == 'f') { expect(s, pos, "false"); return json(false); }
        if (c == '"') return json(parse_string(s, pos));
        if (c == '[') return parse_array(s, pos);
        if (c == '{') return parse_object(s, pos);
        if (isdigit(c) || c == '-') return parse_number(s, pos);
        throw std::invalid_argument("unexpected char");
    }

    static void skip_ws(const std::string& s, size_t& pos) { while (pos < s.size() && isspace(s[pos])) pos++; }
    static void expect(const std::string& s, size_t& pos, const std::string& expected) { if (s.substr(pos, expected.size()) != expected) throw std::invalid_argument("expected " + expected); pos += expected.size(); }

    static std::string parse_string(const std::string& s, size_t& pos)
    {
        pos++;
        std::string res;
        while (pos < s.size() && s[pos] != '"')
        {
            if (s[pos] == '\\') { pos++; if (pos >= s.size()) throw std::invalid_argument("bad escape"); switch (s[pos]) { case '"': res += '"'; break; case '\\': res += '\\'; break; case '/': res += '/'; break; case 'n': res += '\n'; break; case 'r': res += '\r'; break; case 't': res += '\t'; break; default: throw std::invalid_argument("bad escape"); } }
            else res += s[pos];
            pos++;
        }
        pos++;
        return res;
    }

    static json parse_array(const std::string& s, size_t& pos)
    {
        pos++;
        json j = make_array();
        skip_ws(s, pos);
        if (pos < s.size() && s[pos] == ']') { pos++; return j; }
        while (pos < s.size())
        {
            j.push_back(parse_internal(s, pos));
            skip_ws(s, pos);
            if (pos < s.size() && s[pos] == ',') { pos++; skip_ws(s, pos); }
            else if (pos < s.size() && s[pos] == ']') { pos++; break; }
            else throw std::invalid_argument("expected comma or ]");
        }
        return j;
    }

    static json parse_object(const std::string& s, size_t& pos)
    {
        pos++;
        json j = make_object();
        skip_ws(s, pos);
        if (pos < s.size() && s[pos] == '}') { pos++; return j; }
        while (pos < s.size())
        {
            skip_ws(s, pos);
            if (pos >= s.size() || s[pos] != '"') throw std::invalid_argument("expected string");
            std::string key = parse_string(s, pos);
            skip_ws(s, pos);
            if (pos >= s.size() || s[pos] != ':') throw std::invalid_argument("expected colon");
            pos++;
            j[key] = parse_internal(s, pos);
            skip_ws(s, pos);
            if (pos < s.size() && s[pos] == ',') { pos++; skip_ws(s, pos); }
            else if (pos < s.size() && s[pos] == '}') { pos++; break; }
            else throw std::invalid_argument("expected comma or }");
        }
        return j;
    }

    static json parse_number(const std::string& s, size_t& pos)
    {
        size_t start = pos;
        if (s[pos] == '-') pos++;
        while (pos < s.size() && isdigit(s[pos])) pos++;
        bool is_float = false;
        if (pos < s.size() && s[pos] == '.') { is_float = true; pos++; while (pos < s.size() && isdigit(s[pos])) pos++; }
        if (pos < s.size() && (s[pos] == 'e' || s[pos] == 'E')) { is_float = true; pos++; if (pos < s.size() && (s[pos] == '+' || s[pos] == '-')) pos++; while (pos < s.size() && isdigit(s[pos])) pos++; }
        std::string num_str = s.substr(start, pos - start);
        if (is_float) return json(std::stod(num_str));
        return json(std::stoll(num_str));
    }
};

template<> inline bool json::get<bool>() const { if (!is_boolean()) throw std::bad_cast(); return m_bool; }
template<> inline int json::get<int>() const { if (!is_number()) throw std::bad_cast(); return (int)m_num; }
template<> inline long long json::get<long long>() const { if (!is_number()) throw std::bad_cast(); return (long long)m_num; }
template<> inline unsigned long long json::get<unsigned long long>() const { if (!is_number()) throw std::bad_cast(); return (unsigned long long)m_num; }
template<> inline double json::get<double>() const { if (!is_number()) throw std::bad_cast(); return m_num; }
template<> inline std::string json::get<std::string>() const { if (!is_string()) throw std::bad_cast(); return m_str; }

}
