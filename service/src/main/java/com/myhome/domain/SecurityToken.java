package com.myhome.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * Represents a security token entity with attributes for token type, token value,
 * creation and expiry dates, and association with a User entity.
 *
 * - tokenType (SecurityTokenType): represents the type of security token.
 *
 * - token (String): is a unique, non-null string representing a security token.
 *
 * - creationDate (LocalDate): represents the date when the security token was created.
 *
 * - expiryDate (LocalDate): represents the date when the security token expires.
 *
 * - isUsed (boolean): represents a boolean state indicating whether the security
 * token has been used or not.
 *
 * - tokenOwner (User): is a reference to a User entity.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"tokenOwner"})
public class SecurityToken extends BaseEntity {
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SecurityTokenType tokenType;
  @Column(nullable = false, unique = true)
  private String token;
  @Column(nullable = false)
  private LocalDate creationDate;
  @Column(nullable = false)
  private LocalDate expiryDate;
  private boolean isUsed;
  @ManyToOne
  private User tokenOwner;
}
