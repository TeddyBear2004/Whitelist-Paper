package de.teddy.bansystem.database.tables;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "bansystem_token")
public class BansystemToken implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "token_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tokenId;

	public Integer getTokenId(){
		return tokenId;
	}

	public void setTokenId(Integer tokenId){
		this.tokenId = tokenId;
	}

	public String getToken(){
		return token;
	}

	public void setToken(String token){
		this.token = token;
	}

	@Column(name = "token", nullable = false)
	private String token;

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
