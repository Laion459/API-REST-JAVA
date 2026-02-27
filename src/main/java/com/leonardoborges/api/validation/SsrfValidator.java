package com.leonardoborges.api.validation;

import com.leonardoborges.api.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for preventing Server-Side Request Forgery (SSRF) attacks.
 * Validates URLs to ensure they don't target internal/private networks.
 */
@Component
@Slf4j
public class SsrfValidator {
    
    // Private IP ranges are validated in isPrivateIp() method
    
    // Blocked protocols
    private static final Set<String> BLOCKED_PROTOCOLS = new HashSet<>(Arrays.asList(
            "file",
            "gopher",
            "jar",
            "jdbc",
            "ldap",
            "ldaps",
            "rmi"
    ));
    
    // Allowed protocols (whitelist approach)
    private static final Set<String> ALLOWED_PROTOCOLS = new HashSet<>(Arrays.asList(
            "http",
            "https"
    ));
    
    /**
     * Validates a URL to prevent SSRF attacks.
     * 
     * @param urlString The URL string to validate
     * @throws ValidationException if the URL is unsafe
     */
    public void validateUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new ValidationException("URL cannot be null or empty");
        }
        
        try {
            URI uri = new URI(urlString);
            validateProtocol(uri);
            validateHost(uri);
            validateIpAddress(uri);
        } catch (URISyntaxException e) {
            throw new ValidationException("Invalid URL format: " + urlString + " - " + e.getMessage());
        }
    }
    
    /**
     * Validates the protocol of the URI.
     */
    private void validateProtocol(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new ValidationException("URL must have a protocol (http/https)");
        }
        
        scheme = scheme.toLowerCase();
        
        if (BLOCKED_PROTOCOLS.contains(scheme)) {
            throw new ValidationException("Protocol '" + scheme + "' is not allowed for security reasons");
        }
        
        if (!ALLOWED_PROTOCOLS.contains(scheme)) {
            throw new ValidationException("Only HTTP and HTTPS protocols are allowed");
        }
    }
    
    /**
     * Validates the host of the URI.
     */
    private void validateHost(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            throw new ValidationException("URL must have a valid host");
        }
        
        host = host.toLowerCase();
        
        // Block localhost variations
        if (isLocalhost(host)) {
            throw new ValidationException("Localhost and internal addresses are not allowed");
        }
        
        // Block internal domain patterns
        if (isInternalDomain(host)) {
            throw new ValidationException("Internal domain addresses are not allowed");
        }
    }
    
    /**
     * Validates the IP address to ensure it's not a private/internal IP.
     */
    private void validateIpAddress(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return;
        }
        
        try {
            InetAddress address = InetAddress.getByName(host);
            if (isPrivateIp(address)) {
                throw new ValidationException("Private/internal IP addresses are not allowed");
            }
            
            if (isLoopback(address)) {
                throw new ValidationException("Loopback addresses are not allowed");
            }
        } catch (UnknownHostException e) {
            // If we can't resolve the host, we'll allow it but log a warning
            log.warn("Could not resolve host '{}' for SSRF validation", host);
        }
    }
    
    /**
     * Checks if the host is a localhost variation.
     */
    private boolean isLocalhost(String host) {
        return host.equals("localhost") 
                || host.equals("127.0.0.1")
                || host.equals("::1")
                || host.startsWith("localhost.")
                || host.endsWith(".localhost");
    }
    
    /**
     * Checks if the host is an internal domain.
     */
    private boolean isInternalDomain(String host) {
        return host.endsWith(".local")
                || host.endsWith(".internal")
                || host.endsWith(".corp")
                || host.endsWith(".lan")
                || host.equals("0.0.0.0")
                || host.startsWith("192.168.")
                || host.startsWith("10.")
                || (host.startsWith("172.") && isInPrivateRange172(host));
    }
    
    /**
     * Checks if an IP is in the 172.16.0.0/12 private range.
     */
    private boolean isInPrivateRange172(String host) {
        try {
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        } catch (NumberFormatException e) {
            // Not a valid IP format
        }
        return false;
    }
    
    /**
     * Checks if an IP address is private (RFC 1918).
     */
    private boolean isPrivateIp(InetAddress address) {
        byte[] bytes = address.getAddress();
        
        if (bytes.length == 4) {
            // IPv4
            int firstOctet = bytes[0] & 0xFF;
            int secondOctet = bytes[1] & 0xFF;
            
            // 10.0.0.0/8
            if (firstOctet == 10) {
                return true;
            }
            
            // 172.16.0.0/12
            if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) {
                return true;
            }
            
            // 192.168.0.0/16
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }
            
            // 127.0.0.0/8 (loopback)
            if (firstOctet == 127) {
                return true;
            }
            
            // 169.254.0.0/16 (link-local)
            if (firstOctet == 169 && secondOctet == 254) {
                return true;
            }
        } else if (bytes.length == 16) {
            // IPv6
            // Check for private IPv6 ranges
            int firstOctet = bytes[0] & 0xFF;
            int secondOctet = bytes[1] & 0xFF;
            
            // fc00::/7 (unique local addresses)
            if ((firstOctet & 0xFE) == 0xFC) {
                return true;
            }
            
            // fe80::/10 (link-local)
            if ((firstOctet & 0xFF) == 0xFE && (secondOctet & 0xC0) == 0x80) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if an IP address is a loopback address.
     */
    private boolean isLoopback(InetAddress address) {
        return address.isLoopbackAddress();
    }
}
