package org.kepr.hostapi.data

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.kepr.hostapi.config.EMPTY_ADDRESS_NOT_ALLOWED
import org.kepr.hostapi.config.EMPTY_ALIAS_NOT_ALLOWED
import org.kepr.hostapi.config.EMPTY_NAME_NOT_ALLOWED
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity(name="host")
data class Host(@Column(unique=true)  var address: String,
                @Column(unique=true)  var name: String, @ManyToOne(fetch = FetchType.LAZY) @Cascade(CascadeType.PERSIST, CascadeType.MERGE)
 var alias: Alias? = null) {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null

    override fun toString(): String {
        return "name: ${this.name}, address: ${this.address}"
    }

}
