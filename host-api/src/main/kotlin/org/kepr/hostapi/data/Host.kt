package org.kepr.hostapi.data

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity(name="host")
data class Host(@Column(unique=true) @NotBlank var address: String,
                @Column(unique=true) @NotBlank var name: String, @ManyToOne(fetch = FetchType.LAZY) @Cascade(CascadeType.ALL)
var alias: Alias? = null) {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null

    override fun toString(): String {
        return "name: ${this.name}, address: ${this.address}, alias: ${this.alias?.name}"
    }
}