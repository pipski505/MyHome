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
 * Represents a security token with its type, unique identifier, creation and expiry
 * dates, usage status, and association with a user.
 *
 * - tokenType (SecurityTokenType): represents the type of security token.
 *
 * - token (String): is a unique string identifier for the SecurityToken.
 *
 * - creationDate (LocalDate): stores the date on which the security token was created.
 *
 * - expiryDate (LocalDate): represents the date when a security token expires.
 *
 * - isUsed (boolean): represents a boolean status indicating whether a security token
 * has been used.
 *
 * - tokenOwner (User): represents a User.
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
