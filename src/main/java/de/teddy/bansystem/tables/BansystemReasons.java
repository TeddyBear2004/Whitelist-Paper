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
@Table(name = "bansystem_reasons")
public class BansystemReasons implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "reason_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer reasonId;

	@Column(name = "duration")
	private Integer duration;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "reason", nullable = false)
	private String reason;

	@Override
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
			return false;
		BansystemReasons that = (BansystemReasons)o;
		return reasonId != null && Objects.equals(reasonId, that.reasonId);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
}