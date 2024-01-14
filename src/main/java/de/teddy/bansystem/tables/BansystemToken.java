package de.teddy.bansystem.tables;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "bansystem_token")
public class BansystemToken implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "token_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tokenId;

	@Column(name = "token", nullable = false)
	private String token;

	@Column(name = "gamemode", nullable = false)
	private String gamemode;

	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
			return false;
		BansystemToken that = (BansystemToken)o;
		return tokenId != null && Objects.equals(tokenId, that.tokenId);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
}