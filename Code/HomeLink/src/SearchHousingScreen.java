import java.util.Scanner;
import java.util.List;
import java.util.*;

public class SearchHousingScreen {
    private SearchHousingManager manager;

    public void setManager(SearchHousingManager manager) {
        this.manager = manager;
    }

    public void displaySearchForm(SearchHousingForm form, String userID) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n🏡 Φόρμα Αναζήτησης Κατοικίας");
        System.out.println("─────────────────────────────────────");

        System.out.print("📍 Περιοχή ενδιαφέροντος: ");
        form.setLocation(sc.nextLine().trim());

        System.out.print("🏠 Τύπος κατοικίας (π.χ. Studio, Διαμέρισμα): ");
        form.setType(sc.nextLine().trim());

        System.out.print("👥 Επιθυμείτε δυνατότητα συγκατοίκησης; (ναι/όχι): ");
        String input = sc.nextLine().trim().toLowerCase();
        form.setCanShare(input.equals("ναι"));

        // Καλείται αμέσως μετά για έξτρα κριτήρια
        fillSearchCriteria(form,userID);
    }


    public void fillSearchCriteria(SearchHousingForm form,String userID) {
        Scanner sc = new Scanner(System.in);

        System.out.print("🛏️ Επιθυμητός αριθμός δωματίων: ");
        try {
            form.setRooms(Integer.parseInt(sc.nextLine().trim()));
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Μη έγκυρη είσοδος. Ορίστηκε αριθμός δωματίων σε 0.");
            form.setRooms(0);
        }

        System.out.println("─────────────────────────────────────");
        System.out.println("🔎 Εκτέλεση αναζήτησης...\n");

        // ✅ Κατευθείαν προώθηση στο search handler
        manager.handleSearchCriteria(form, userID);
    }

    public void displayMessage(String msg) {
        System.out.println("[Μήνυμα]: " + msg);
    }

    public void askToApplyPreferences(UserPreferences prefs, SearchHousingForm form, String userID) {
        System.out.println("📁 Εντοπίστηκαν αποθηκευμένες προτιμήσεις:");
        System.out.println("- Περιοχή: " + prefs.getLocation());
        System.out.println("- Τύπος: " + prefs.getType());
        System.out.println("- Συγκατοίκηση: " + (prefs.isCanShare() ? "Ναι" : "Όχι"));
        System.out.print("❓ Θέλετε να τις εφαρμόσουμε; (ναι/όχι): ");

        Scanner sc = new Scanner(System.in);
        String answer = sc.nextLine().trim().toLowerCase();

        if (answer.equals("ναι")) {
            acceptFilters(prefs, userID);
        } else {
            declineFilters(form, userID);
        }
    }

    public void acceptFilters(UserPreferences prefs, String userID) {
        manager.applyPreferences(prefs, userID);
    }

    public void declineFilters(SearchHousingForm form, String userID) {
        manager.continueWithoutPreferences(form, userID);
    }


    public void displayMap(List<Marker> markers) {
        System.out.println("\n🗺️ Χάρτης Αγγελιών:");
        for (Marker m : markers) {
            System.out.println(m);
        }
    }

    public void displaySearchResults(List<Listing> listings, List<Marker> markers) {
        displayMap(markers);

        System.out.println("\n🔍 Αποτελέσματα Αναζήτησης:");
        for (Listing l : listings) {
            System.out.println("ID: " + l.getId() + " → " + l + " [Score: " + l.getScore() + "]");
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("\nΠληκτρολογήστε ID αγγελίας για προβολή ή 'έξοδος' για έξοδο: ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("έξοδος")) {
                System.out.println("Έξοδος από τα αποτελέσματα.");
                break;
            }

            onListingSelected(input); // χωρίς μετατροπή
        }
    }


    public void onListingSelected(String listingID) {
        RentalInterestManager interestManager = new RentalInterestManager();
        Listing fullDetails = interestManager.fetchFullListingDetails(listingID);

        if (fullDetails != null) {
            displayDetailedListing(fullDetails);
        } else {
            displayMessage("⚠️ Η αγγελία δεν βρέθηκε ή έχει αφαιρεθεί.");
        }
    }


    public void displayDetailedListing(Listing listing) {
        System.out.println("\n📄 Λεπτομέρειες Αγγελίας:");
        System.out.println("Τύπος: " + listing.getType());
        System.out.println("Owner: " + listing.getOwnerID());
        System.out.println("Τιμή: " + listing.getPrice());
        System.out.println("Μέγεθος: " + listing.getSize() + " τ.μ.");
        System.out.println("Δωμάτια: " + listing.getRooms());
        System.out.println("Όροφος: " + listing.getFloor());
        System.out.println("Διεύθυνση: " + listing.getAddress());

        Scanner sc = new Scanner(System.in);
        System.out.print("\nΘέλετε να δηλώσετε ενδιαφέρον για αυτή την αγγελία; (ναι/όχι): ");
        String input = sc.nextLine().trim().toLowerCase();

        if (input.equals("ναι")) {
            System.out.print("Πληκτρολογήστε το userID σας: ");
            String userID = sc.nextLine().trim(); // Αν δεν είναι ήδη γνωστό

            submitInterest(userID, listing);
        }
    }

    public void submitInterest(String userID, Listing listing) {
        RentalInterestManager manager = new RentalInterestManager();
        String listingID = listing.getId();

        if (manager.checkExistingInterest(userID, listingID)) {
            Map<String, String> existing = manager.getInterestDetails(userID, listingID);
            displayExistingInterest(userID, listing, existing);
        } else {
            Scanner sc = new Scanner(System.in);
            System.out.print("Θέλετε να προσθέσετε κάποιο μήνυμα στον ιδιοκτήτη; (προαιρετικό): ");
            String message = sc.nextLine().trim();

            manager.createInterest(userID, listingID, message);
            displayMessage("✅ Το ενδιαφέρον σας καταχωρήθηκε επιτυχώς.");

            // 🔁 Κλήση της οθόνης εισαγωγής διαθεσιμότητας
            UserAvailabilityScreen.showAvailabilitySelectionScreen(userID, listingID);
        }
    }



    public void displayExistingInterest(String userID, Listing listing, Map<String, String> interestDetails) {
        System.out.println("\n📌 Έχετε ήδη δηλώσει ενδιαφέρον για αυτή την αγγελία:");
        System.out.println("🕒 Ημερομηνία: " + interestDetails.get("timestamp"));
        System.out.println("📨 Μήνυμα: " + (interestDetails.get("message") == null || interestDetails.get("message").isEmpty()
                ? "(χωρίς μήνυμα)" : interestDetails.get("message")));

        Scanner sc = new Scanner(System.in);
        System.out.print("\nΘέλετε να διατηρήσετε ή να ακυρώσετε το ενδιαφέρον; (διατήρηση/ακύρωση): ");
        String input = sc.nextLine().trim().toLowerCase();

        if (input.equals("ακύρωση")) {
            boolean deleted = ManageDB.deleteInterest(userID, listing.getId());

            if (deleted) {
                String ownerID = listing.getOwnerID();  // Παίρνουμε απευθείας από το listing
                String emailBody = EmailService.createCancellationEmail(userID, listing.getId());

                EmailService.sendEmail(ownerID, emailBody);

                Message.createSuccessMessage("Interest Cancellation Completed.");
            } else {
                Message.createErrorMessage("Η ακύρωση απέτυχε. Δοκιμάστε ξανά.");
            }
        } else {
            System.out.println("✅ Το ενδιαφέρον διατηρείται.");
        }
    }




}
