package com.clientui.clientui.controller;

import com.clientui.clientui.beans.CommandeBean;
import com.clientui.clientui.beans.PaiementBean;
import com.clientui.clientui.beans.ProductBean;
import com.clientui.clientui.proxies.MicroserviceCommandeProxy;
import com.clientui.clientui.proxies.MicroservicePaiementProxy;
import com.clientui.clientui.proxies.MicroserviceProduitsProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class ClientController {
    @Autowired
    MicroserviceProduitsProxy produitsProxy;

    @Autowired
    MicroserviceCommandeProxy commandesProxy;

    @Autowired
    MicroservicePaiementProxy paiementProxy;

    @RequestMapping("/")
    public String accueil(Model model){
        List<ProductBean> produits =  produitsProxy.listeDesProduits();
        model.addAttribute("produits", produits);
        return "Accueil";
    }

    @RequestMapping("/details-produit/{id}")
    public String ficheProduit(@PathVariable int id,  Model model){
        ProductBean produit = produitsProxy.recupererUnProduit(id);
        model.addAttribute("produit", produit);
        return "FicheProduit";
    }

    @RequestMapping(value = "details-produit/commander-produit/{idProduit}/{montant}")
    public String passerCommande(@PathVariable int idProduit, @PathVariable Integer montant,  Model model){

        CommandeBean commande = new CommandeBean();
        ProductBean produit = new ProductBean();
        //On renseigne les propriétés de l'objet de type CommandeBean que nous avons créee
        produit.setPrix(12);
        commande.setProductId(idProduit);
        commande.setQuantite(1);
        commande.setDateCommande(new Date());

        //appel du microservice commandes grâce à Feign et on récupère en retour les détails de la commande créée, notamment son ID (étape 4).
        CommandeBean commandeAjoutee = commandesProxy.ajouterCommande(commande);

        //on passe à la vue l'objet commande et le montant de celle-ci afin d'avoir les informations nécessaires pour le paiement
        model.addAttribute("commande", commandeAjoutee);
        model.addAttribute("montant", montant);

        return "Paiement";
    }

    @RequestMapping(value = "/payer-commande/{idCommande}/{montantCommande}")
    public String payerCommande(@PathVariable int idCommande, @PathVariable Integer montantCommande, Model model){

        PaiementBean paiementAExcecuter = new PaiementBean();

        //on renseigne les détails du produit
        paiementAExcecuter.setIdCommande(idCommande);
        paiementAExcecuter.setMontant(montantCommande);
        paiementAExcecuter.setNumeroCarte(numcarte()); // on génère un numéro au hasard pour simuler une CB

        // On appelle le microservice et (étape 7) on récupère le résultat qui est sous forme ResponseEntity<PaiementBean> ce qui va nous permettre de vérifier le code retour.
        ResponseEntity<PaiementBean> paiement = paiementProxy.payerUneCommande(paiementAExcecuter);

        Boolean paiementAccepte = false;
        //si le code est autre que 201 CREATED, c'est que le paiement n'a pas pu aboutir.
        if(paiement.getStatusCode() == HttpStatus.CREATED)
            paiementAccepte = true;

        model.addAttribute("paiementOk", paiementAccepte); // on envoie un Boolean paiementOk à la vue

        return "Confirmation";
    }

    //Génére une serie de 16 chiffres au hasard pour simuler vaguement une CB
    private Long numcarte() {

        return ThreadLocalRandom.current().nextLong(1000000000000000L,9000000000000000L );
    }
}