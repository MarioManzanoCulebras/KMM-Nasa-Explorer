//
//  Image.swift
//  iosApp
//
//  Created by Mario Manzano Culebras on 27/12/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared
import SwiftUI

extension Image {
    init(resource: KeyPath<MR.images, shared.ImageResource>) {
        self.init(uiImage: MR.images()[keyPath: resource].toUIImage()!)
    }
}
