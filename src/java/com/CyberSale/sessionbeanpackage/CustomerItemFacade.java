/*
 * Created by Patrick Abod on 2016.04.19  * 
 * Copyright © 2016 Patrick Abod. All rights reserved. * 
 */
package com.CyberSale.sessionbeanpackage;

import com.CyberSale.entitypackage.CustomerItem;
import com.CyberSale.entitypackage.Item;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author patrickabod
 */
@Stateless
public class CustomerItemFacade extends AbstractFacade<CustomerItem> {

    @PersistenceContext(unitName = "CyberSalePU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CustomerItemFacade() {
        super(CustomerItem.class);
    }
    
    
    /*
        Methods added to generated code
    */
    
    public List<Item> findRecentItems() {
        List<Item> items = em.createQuery("SELECT i FROM Item i ORDER BY i.postedDate DESC").getResultList(); 

        if (items.isEmpty())
            return null;
        else
            return items;
    }
    
    public List<Item> findPopularItems() {
        List<Item> items = em.createQuery("SELECT i FROM Item i ORDER BY i.hits DESC").getResultList(); 

        if (items.isEmpty())
            return null;
        else
            return items;
    }
    
    public List<Item> findCheapItems() {
        List<Item> items = em.createQuery("SELECT i FROM Item i ORDER BY i.cost ASC").getResultList(); 

        if (items.isEmpty())
            return null;
        else
            return items;
    }
}
