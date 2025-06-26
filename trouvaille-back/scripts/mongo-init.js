// MongoDB initialization script for Trouvaille application
// Note: Collections and most indexes are managed by Liquibase
// This script only handles MongoDB-specific tasks that can't be done via Liquibase

print('Starting MongoDB initialization for Trouvaille...');

// Switch to trouvaille database
db = db.getSiblingDB('trouvaille');

// Create application user with appropriate permissions
// (Only if running in a secured MongoDB environment)
try {
    db.createUser({
        user: "trouvaille_app",
        pwd: "trouvaille_app_password", // Should be changed in production
        roles: [
            {
                role: "readWrite",
                db: "trouvaille"
            }
        ]
    });
    print('Application user created successfully');
} catch (e) {
    if (e.code === 51003) { // User already exists
        print('Application user already exists, skipping creation');
    } else {
        print('Warning: Could not create application user: ' + e.message);
    }
}

// MongoDB-specific configuration that can't be handled by Liquibase
db.runCommand({
    collMod: "Annonce",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["type", "nature", "titre", "description", "coordinates"],
            properties: {
                type: {
                    bsonType: "string",
                    enum: ["vente", "location"]
                },
                nature: {
                    bsonType: "string", 
                    enum: ["offre", "demande"]
                },
                titre: {
                    bsonType: "string",
                    minLength: 5,
                    maxLength: 100
                },
                description: {
                    bsonType: "string",
                    maxLength: 2000
                },
                prix: {
                    bsonType: ["double", "int"],
                    minimum: 0
                },
                statut: {
                    bsonType: "string",
                    enum: ["active", "suspendue", "vendue"]
                },
                coordinates: {
                    bsonType: "object",
                    required: ["latitude", "longitude"],
                    properties: {
                        latitude: {
                            bsonType: ["double", "int"],
                            minimum: -90,
                            maximum: 90
                        },
                        longitude: {
                            bsonType: ["double", "int"],
                            minimum: -180,
                            maximum: 180
                        }
                    }
                },
                photos: {
                    bsonType: "array",
                    maxItems: 10,
                    items: {
                        bsonType: "string"
                    }
                }
            }
        }
    },
    validationLevel: "moderate", // Allow existing documents that don't match
    validationAction: "warn"     // Log validation errors but don't reject
});

print('Schema validation configured for Annonce collection');

// Set up database-level settings
db.runCommand({
    profile: 1,     // Enable profiling for slow operations
    slowms: 1000    // Log operations slower than 1 second
});

print('Database profiling enabled for performance monitoring');

// Create capped collection for application logs if needed
try {
    db.createCollection("app_logs", {
        capped: true,
        size: 10485760, // 10MB
        max: 10000      // Max 10k documents
    });
    print('Capped collection for application logs created');
} catch (e) {
    if (e.code === 48) { // Collection already exists
        print('App logs collection already exists');
    } else {
        print('Warning: Could not create app logs collection: ' + e.message);
    }
}

print('MongoDB initialization completed successfully.');
print('Note: Collections and indexes are managed by Liquibase during application startup.');