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
@Table(name = "bansystem_reasons")
public class BansystemReasons implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	public Integer getReasonId(){
		return reasonId;
	}

	public void setReasonId(Integer reasonId){
		this.reasonId = reasonId;
	}

	public Integer getDuration(){
		return duration;
	}

	public void setDuration(Integer duration){
		this.duration = duration;
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getReason(){
		return reason;
	}

	public void setReason(String reason){
		this.reason = reason;
	}

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
