package org.kepr.hostapi.data

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.kepr.hostapi.config.EMPTY_NAME_NOT_ALLOWED
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name="alias")
data class Alias(@Column(unique=true) @field:NotBlank(message = EMPTY_NAME_NOT_ALLOWED) var name: String, @OneToMany(mappedBy = "alias", fetch = FetchType.LAZY) @Cascade(CascadeType.PERSIST, CascadeType.MERGE)
var hosts: MutableList<Host>) {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null

    override fun toString(): String {
       return "name: ${this.name}}"
    }
}
